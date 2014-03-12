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


- (void)receivedMessage:(NSNotification*)notification;
- (void)receivedChatroom:(NSNotification*)notification;
- (void)receivedJoinedChatroom:(NSNotification*)notification;
- (void)receivedLeftChatroom:(NSNotification*)notification;
- (void)receivedInviteUser:(NSNotification*)notification;
- (void)registerForNotifications;
- (void)unregisterForNotifications;

@end


@implementation ChatroomManagement
{
    UserDetails* ud;
    Location* location;
    
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
        NSLog(@"Chatroom is too far away");
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


#pragma mark - Managing chatroom memberships

// Dispatch the sending of the message
//- (void)joinChatroomWithId:(long long)chatId latitude:(long long)lat longitude:(long long)lng
//{
//    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
//    msg.userId = ud.userId;
//    msg.chatroomId = chatId;
//    msg.latitude = [location currentLat];
//    msg.longitude = [location currentLong];
//}


#pragma mark - Messages

- (void)receivedMessage:(NSNotification*)notification
{
    MessageMessage* message = notification.object;
    Chatroom* c;
    if ((c = [_chatrooms objectForKey:[NSNumber numberWithLongLong:message.chatroomId]])) {
        NSLog(@"message got");
        [[c mutableArrayValueForKey:@"chatQueue"] addObject:message];
    }
}

// NOTE: We don't need to worry about receiving duplicates because
// duplicates will be overwritten in the Dictionary.
- (void)receivedChatroom:(NSNotification*)notification
{
    ChatroomMessage* message = notification.object;
    Chatroom* c = [Chatroom chatroomWithChatroomMessage:message];
    if ([self canJoinChatroom:c]) {
        if (![_chatrooms objectForKey:c.cid]) {
            if (c.isGlobal) {
                [_globalChatrooms addObject:c];
            }
            else {
                [_localChatrooms addObject:c];
            }
            [_chatrooms setObject:c forKey:c.cid];
        }
    }
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

- (void)receivedInviteUser:(NSNotification*)notification
{
    // TODO: if this chatroom is not already in chatrooms list, add it--
    // this way, private that this user was invited to will show up
}





@end
