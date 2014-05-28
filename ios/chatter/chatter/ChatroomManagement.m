//
//  ChatroomManagement.m
//  chatter
//
//  Created by sean matthews on 2/4/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

/**
 * This class is for managing the user's joined chatrooms and the messages,
 * the messages he receives from those chatrooms, and monitoring to see if
 * he has traveled outside the range of those chatrooms.
 */

#import "ChatroomManagement.h"
#import "Connection.h"
#import "UIInviteAlertView.h"
#import "Connection.h"

@interface ChatroomManagement()

- (id)init;
- (void)addChatroom:(Chatroom*)chatroom;
- (void)removeChatroomWithId:(NSNumber*)chatroomId;
- (void)receivedMessage:(NSNotification*)notification;
- (void)receivedChatroom:(NSNotification*)notification;
- (void)receivedJoinedChatroom:(NSNotification*)notification;
- (void)receivedJoinChatroomReject:(NSNotification*)notification;
- (void)receivedLeftChatroom:(NSNotification*)notification;
- (void)receivedInviteUser:(NSNotification*)notification;
- (void)receivedLoginAccept:(NSNotification*)notification;
- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)displayInvite:(InviteUserMessage*)message toChatroom:(Chatroom*)chatroom;
- (void)dismissAllInviteAlerts;
- (void)createChatroom:(Chatroom*)chatroom withCompletion:(CreateCompletion)completion;
- (void)loadJoinedChatroomsAtStartup;


@end


@implementation ChatroomManagement
{
    Connection* connection;
    UserDetails* ud;
    Location* location;
    NSMutableArray* inviteAlerts;
    JoinCompletion joinCompletion;
    CreateCompletion createCompletion;
    UIBackgroundTaskIdentifier leaveChatroomsTask;
    NSInteger joinedCount;
}

- (id)init
{
    self = [super init];
    
    if (self) {
        joinedCount = 0;
        leaveChatroomsTask = UIBackgroundTaskInvalid;
        joinCompletion = nil;
        createCompletion = nil;
        connection = [Connection sharedInstance];
        _chatrooms = [[NSMutableDictionary alloc] init];
        _globalChatrooms = [[NSMutableArray alloc] init];
        _localChatrooms = [[NSMutableArray alloc] init];
        _joinedChatrooms = [[NSMutableArray alloc] init];
        inviteAlerts = [[NSMutableArray alloc] init];
        ud = [UserDetails sharedInstance];
        location = [Location sharedInstance];
        [self registerForNotifications];
    }
    return self;
}

+ (id)sharedInstance
{
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
        // Additional initialization can go here
    });
    return _sharedObject;
}

- (void)dealloc
{
    [self unregisterForNotifications];
}

// Load chatrooms that were joined previously
- (void)loadJoinedChatroomsAtStartup
{
    NSLog(@"[ChatroomManagement] Loading joined chatrooms");
    if (ud.joinedChatroomDicts != nil) {
        NSLog(@"%@",ud.joinedChatroomDicts);
        for (NSDictionary* chatroom in ud.joinedChatroomDicts) {
            Chatroom* c = [Chatroom chatroomWithDictionary:chatroom];
            if ([self canJoinChatroom:c] && [_chatrooms objectForKey:c.cid]) {
                
                JoinChatroomMessage * msg = [[JoinChatroomMessage alloc] init];
                msg.userId = ud.userId;
                msg.chatroomId = [c.cid longLongValue];
                msg.latitude = [location currentLat];
                msg.longitude = [location currentLong];
                [connection sendMessage:msg];
                
                // Took this out because I'm changing the server to sens back a
                // joined message, even if already joined.
//                [[self mutableArrayValueForKey:@"joinedChatrooms"] insertObject:[_chatrooms objectForKey:c.cid] atIndex:0];
            }
            else {
                // Don't add unjoinable chatrooms to the joined list,
                // and also leave them on the server side.
                [self leaveChatroomWithId:c.cid withCompletion:nil];
            }
        }
    }
//    for (NSNumber *chatroomId in ud.joinedChatroomIds) {
//        NSLog(@"joining saved chatroom %@",chatroomId);
//        JoinChatroomMessage * msg = [[JoinChatroomMessage alloc] init];
//        msg.userId = ud.userId;
//        msg.chatroomId = [chatroomId longLongValue];
//        msg.latitude = [location currentLat];
//        msg.longitude = [location currentLong];
//        [connection sendMessage:msg];
//    }
}


- (void)registerForNotifications
{
    for (NSString* notificationName in @[@"Message", @"JoinedChatroom", @"JoinChatroomReject",
                                         @"LeftChatroom", @"InviteUser", @"Chatroom",
                                         @"CreateChatroomReject", @"LoginAccept"]) {
        NSString* selectorName = [NSString stringWithFormat:@"received%@:",notificationName];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:NSSelectorFromString(selectorName)
                                                     name:[NSString stringWithFormat:@"%@Message",notificationName]
                                                   object:nil];
    }
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - Convenience


- (BOOL)canJoinChatroomWithCoord:(CLLocationCoordinate2D)coord andRadius:(long long)radius
{
    NSUInteger distance = [location metersToCurrentLocationFrom:coord];
    
    // Only display local chatrooms that the user is able to join
    if (distance - radius > 0) {
        NSLog(@"[chat management] Chatroom is too far away");
        return NO;
    }
    return YES;
}

- (BOOL)canJoinChatroom:(Chatroom*)chatroom
{
    if (chatroom.isGlobal) {
        return YES;
    }
    else {
        return [self canJoinChatroomWithCoord:chatroom.origin
                                    andRadius:[chatroom.radius longLongValue]];

    }
}

- (void)addChatroom:(Chatroom*)chatroom
{
    if ([self canJoinChatroom:chatroom]) {
        if (![_chatrooms objectForKey:chatroom.cid]) {
            if (chatroom.isGlobal) {
                [[self mutableArrayValueForKey:@"globalChatrooms"] addObject:chatroom];
            }
            else {
                [[self mutableArrayValueForKey:@"localChatrooms"] addObject:chatroom];
            }
            
            [_chatrooms setObject:chatroom forKey:chatroom.cid];
        }
    }
}

- (void)removeChatroomWithId:(NSNumber*)chatroomId
{
    Chatroom* c = [_chatrooms objectForKey:chatroomId];
    if (c) {
        if (c.isGlobal) {
            [[self mutableArrayValueForKey:@"globalChatrooms"] removeObject:c];
        }
        else {
            [[self mutableArrayValueForKey:@"localChatrooms"] removeObject:c];
        }
        [_chatrooms removeObjectForKey:chatroomId];
    }
}

- (void)removeChatroom:(Chatroom*)chatroom
{
    if ([_chatrooms objectForKey:chatroom.cid]) {
        if (chatroom.isGlobal) {
            [[self mutableArrayValueForKey:@"globalChatrooms"] removeObject:chatroom];
        }
        else {
            [[self mutableArrayValueForKey:@"localChatrooms"] removeObject:chatroom];
        }
        [_chatrooms removeObjectForKey:chatroom.cid];
    }
}

- (void)leaveJoinedChatrooms
{
    NSArray* pastJoined = [NSArray arrayWithArray:_joinedChatrooms];
    [[self mutableArrayValueForKey:@"joinedChatrooms"] removeAllObjects];
    
    if ([pastJoined count] > 0) {
        joinedCount = [pastJoined count];
        leaveChatroomsTask = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
            leaveChatroomsTask = UIBackgroundTaskInvalid;
        }];
    }
    
    for (Chatroom* c in pastJoined) {
        LeaveChatroomMessage* lcm = [[LeaveChatroomMessage alloc] init];
        lcm.userId = ud.userId;
        lcm.chatroomId = [c.cid longLongValue];
        NSLog(@"leaving %llu",lcm.chatroomId);
        [connection sendMessage:lcm];
    }
}


#pragma mark - Messages

- (void)searchChatrooms
{
    SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    msg.onlyJoinable = YES;
    msg.metersFromCoords = 0;
    [connection sendMessage:msg];
}

- (void)joinChatroom:(Chatroom*)chatroom withCompletion:(JoinCompletion)completion
{
    [self joinChatroomWithId:chatroom.cid withCompletion:completion];
}

- (void)joinChatroomWithId:(NSNumber*)chatroomId withCompletion:(JoinCompletion)completion
{
    Chatroom* c = [_chatrooms objectForKey:chatroomId];
    
    if ([_joinedChatrooms containsObject:c]) {
        [[self mutableArrayValueForKey:@"joinedChatrooms"] removeObject:c];
        [[self mutableArrayValueForKey:@"joinedChatrooms"] insertObject:c atIndex:0];
        
        if (completion) {
            completion();
        }
    }
    else {
        JoinChatroomMessage* jcm = [[JoinChatroomMessage alloc] init];
        jcm.chatroomId = [chatroomId longLongValue];
        jcm.latitude = [location currentLat];
        jcm.longitude = [location currentLong];
        jcm.userId = ud.userId;
        [connection sendMessage:jcm];
        joinCompletion = completion;
    }
}

- (void)leaveChatroomWithId:(NSNumber*)chatroomId withCompletion:(LeaveCompletion)completion
{
    // Don't check beforehand if we're joined to the chatroom--
    // sometimes we want to send a just-in-case leave chatroom message.
    
    LeaveChatroomMessage* lcm = [[LeaveChatroomMessage alloc] init];
    lcm.userId = ud.userId;
    lcm.chatroomId = [chatroomId longLongValue];
    [connection sendMessage:lcm];
    
    // If the chatroom is in the list of joined chatrooms, remove it
    Chatroom* c = [_chatrooms objectForKey:chatroomId];
    if (c) {
        [[self mutableArrayValueForKey:@"joinedChatrooms"] removeObject:c];
    }
}

- (void)createChatroom:(Chatroom*)chatroom withCompletion:(CreateCompletion)completion
{
    CreateChatroomMessage* ccm = [[CreateChatroomMessage alloc] init];
    ccm.ownerId = ud.userId;
    ccm.chatroomName = chatroom.chatroomName;
    ccm.isPrivate = chatroom.isPrivate;
    ccm.latitude = [location currentLat];
    ccm.longitude = [location currentLong];
    ccm.radius = [chatroom.radius longLongValue];
    [connection sendMessage:ccm];
    createCompletion = completion;
}


#pragma mark - Notifications

- (void)receivedLoginAccept:(NSNotification*)notification
{
    // Uncomment this if the server removes users from chatroom when the user app is terminated.
    // This would be the case if the client app sends the terminate message upon termination.
    [self performSelector:@selector(loadJoinedChatroomsAtStartup) withObject:nil afterDelay:1.0];
}

- (void)receivedMessage:(NSNotification*)notification
{
    MessageMessage* message = notification.object;
    Chatroom* c;
    if ((c = [_chatrooms objectForKey:[NSNumber numberWithLongLong:message.chatroomId]])) {
        NSLog(@"message message added");
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

// NOTE: We don't need to worry about receiving duplicates because
// duplicates will be overwritten in the Dictionary.
- (void)receivedChatroom:(NSNotification*)notification
{
    ChatroomMessage* message = notification.object;
    Chatroom* c = [Chatroom chatroomWithChatroomMessage:message];
    [self addChatroom:c];
    
    if (createCompletion) {
        createCompletion([c.cid longLongValue]);
        createCompletion = nil;
    }
}

- (void)receivedJoinedChatroom:(NSNotification*)notification
{
    JoinedChatroomMessage* message = notification.object;
    Chatroom* c = [_chatrooms objectForKey:[NSNumber numberWithLongLong:message.chatroomId]];
    if (message.userId == ud.userId) {
        NSLog(@"joined");
        if ([_joinedChatrooms containsObject:c]) {
            [[self mutableArrayValueForKey:@"joinedChatrooms"] removeObject:c];
        }
        [[self mutableArrayValueForKey:@"joinedChatrooms"] insertObject:c atIndex:0];
        if (joinCompletion) {
            joinCompletion();
            joinCompletion = nil;
        }
    }
    else {
        NSLog(@"joined message added");
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

- (void)receivedLeftChatroom:(NSNotification*)notification
{
    LeftChatroomMessage* message = notification.object;
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    if (message.userId == ud.userId) {
        [[self mutableArrayValueForKey:@"joinedChatrooms"] removeObject:[_chatrooms objectForKey:cid]];
        
        // If we tried to leave all the chatrooms, it started a background task
        if (leaveChatroomsTask != UIBackgroundTaskInvalid) {
            if (--joinedCount <= 0) {
                [[UIApplication sharedApplication] endBackgroundTask:leaveChatroomsTask];
            }
        }
    }
    else {
        Chatroom* c = [_chatrooms objectForKey:cid];
        NSLog(@"left message added");
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

// Received an invite to a chatroom from another user
- (void)receivedInviteUser:(NSNotification*)notification
{
    if (ud.receiveInviteNotifications) {
        // If the chatroom is not in out list-- which means it's private--
        // add it to our list.
        if (![_chatrooms objectForKey:[NSNumber numberWithLongLong:[notification.object chatroomId]]]) {
            // Chatroom is not in our list. It's either a private chatroom, or out of our range.
            Chatroom* c = [Chatroom chatroomWithInviteUserMessage:notification.object];
            
            // Check that the user can join this chatroom. If so, add it to the list.
            [self addChatroom:c];
        }
        
        Chatroom* invitedChatroom = [_chatrooms objectForKey:[NSNumber numberWithLongLong:[notification.object chatroomId]]];
        [self displayInvite:notification.object toChatroom:invitedChatroom];
    }
}

- (void)receivedJoinChatroomReject:(NSNotification*)notification
{
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Woops!"
                                                    message:[notification.object reason]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
    [self removeChatroomWithId:[NSNumber numberWithLongLong:[notification.object chatroomId]]];
}

- (void)receivedCreateChatroomReject:(NSNotification*)notification
{
    
    
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle: @"Woops!"
                                                    message:[notification.object reason]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}


#pragma mark - UIAlertViewDelegate et al

// Display an invite alert, with an attached chatroom object.
- (void)displayInvite:(InviteUserMessage*)message toChatroom:(Chatroom*)chatroom
{
    NSString* alertMsg = [NSString stringWithFormat:@"%@ has invited you to chatroom %@",
                          message.senderHandle,message.chatroomName];
    NSLog(@"%@",alertMsg);
    UIInviteAlertView* alert = [[UIInviteAlertView alloc] initWithTitle:@"Invitation!"
                                                                message:alertMsg
                                                               delegate:self
                                                      cancelButtonTitle:nil
                                                      otherButtonTitles:@"Join",@"Decline",nil];
    alert.chatroom = chatroom;
    [inviteAlerts addObject:alert];
    [alert show];
}

- (void)dismissAllInviteAlerts
{
    for (UIAlertView* alert in inviteAlerts) {
        [alert dismissWithClickedButtonIndex:1 animated:YES];
    }
}

// If the user decides to join the chatroom, send a notification to the current view.
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    // 0 == JOIN
    if (0 == buttonIndex) {
        Chatroom* c = ((UIInviteAlertView*)alertView).chatroom;
        
        // Join the chatroom
        [self joinChatroom:c withCompletion:nil];
        
        [self dismissAllInviteAlerts];
        
        // Send segue notification to current view
        [[NSNotificationQueue defaultQueue] enqueueNotification:[NSNotification
                                                                 notificationWithName:@"segueToChatroomNotification"
                                                                               object:c]
                                                   postingStyle:NSPostNow];
    }
}


@end
