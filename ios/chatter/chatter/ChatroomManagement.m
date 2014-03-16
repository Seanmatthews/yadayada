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

@interface ChatroomManagement()

- (id)init;

- (void)addChatroom:(Chatroom*)chatroom;
- (void)receivedMessage:(NSNotification*)notification;
- (void)receivedChatroom:(NSNotification*)notification;
- (void)receivedJoinedChatroom:(NSNotification*)notification;
- (void)receivedLeftChatroom:(NSNotification*)notification;
- (void)receivedInviteUser:(NSNotification*)notification;
- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)displayInvite:(InviteUserMessage*)message toChatroom:(Chatroom*)chatroom;
- (void)dismissAllInviteAlerts;

@end


@implementation ChatroomManagement
{
    UserDetails* ud;
    Location* location;
    NSMutableArray* inviteAlerts;
}

//const int MESSAGE_NUM_THRESH = 20;

- (id)init
{
    self = [super init];
    
    if (self) {
        _chatrooms = [[NSMutableDictionary alloc] init];
        _globalChatrooms = [[NSMutableArray alloc] init];
        _localChatrooms = [[NSMutableArray alloc] init];
        _joinedChatrooms = [[NSMutableArray alloc] init];
        inviteAlerts = [[NSMutableArray alloc] init];
        ud = [UserDetails sharedInstance];
        location = [Location sharedInstance];
        _goingToJoin = nil;
        _createdToJoin = nil;
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

- (void)registerForNotifications
{
    for (NSString* notificationName in @[@"Message", @"JoinedChatroom",
                                         @"LeftChatroom", @"InviteUser", @"Chatroom"]) {
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

// NOTE: Assumes one joined chatroom
- (Chatroom*)currentChatroom
{
    return[[_joinedChatrooms objectEnumerator] nextObject];
}

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
        return [self canJoinChatroomWithCoord:chatroom.origin andRadius:[chatroom.radius longLongValue]];

    }
}

- (void)addChatroom:(Chatroom*)chatroom
{
    if ([self canJoinChatroom:chatroom]) {
        if (![_chatrooms objectForKey:chatroom.cid]) {
            if (chatroom.isGlobal) {
                [_globalChatrooms addObject:chatroom];
            }
            else {
                [_localChatrooms addObject:chatroom];
            }
            [_chatrooms setObject:chatroom forKey:chatroom.cid];
        }
    }
}


#pragma mark - Managing chatroom memberships



#pragma mark - Messages

- (void)receivedMessage:(NSNotification*)notification
{
    MessageMessage* message = notification.object;
    Chatroom* c;
    if ((c = [_chatrooms objectForKey:[NSNumber numberWithLongLong:message.chatroomId]])) {
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
}

- (void)receivedJoinedChatroom:(NSNotification*)notification
{
    JoinedChatroomMessage* message = notification.object;
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    if (message.userId == ud.userId) {
        [_joinedChatrooms addObject:[_chatrooms objectForKey:cid]];
    }
    else {
        Chatroom* c = [_chatrooms objectForKey:cid];
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

- (void)receivedLeftChatroom:(NSNotification*)notification
{
    LeftChatroomMessage* message = notification.object;
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    if (message.userId == ud.userId) {
        [_joinedChatrooms removeObject:[_chatrooms objectForKey:cid]];
    }
    else {
        Chatroom* c = [_chatrooms objectForKey:cid];
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

// Received an invite to a chatroom from another user
- (void)receivedInviteUser:(NSNotification*)notification
{
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
        // Send segue notification to current view
        [[NSNotificationQueue defaultQueue] postNotificationName:@"segueToChatroomNotification"
                                                          object:((UIInviteAlertView*)alertView).chatroom];
    }
}


@end
