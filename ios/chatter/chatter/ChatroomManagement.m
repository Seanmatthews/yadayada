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

- (void)addMessage:(MessageBase*)message toChatroom:(NSNumber*)chatroomId;
- (void)dismissAllInviteAlerts;
- (void)receivedMessage:(MessageMessage*) message;
- (void)receivedJoinedChatroom:(JoinedChatroomMessage*)message;
- (void)receivedLeftChatroom:(LeftChatroomMessage*)message;
- (void)showInviteAlert:(InviteUserMessage*)ium;

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
        _MESSAGE_NUM_THRESH = 50;
        _chatQueue = [[NSMutableDictionary alloc] init];
        _joinedChatrooms = [[NSMutableDictionary alloc] init];
        ud = [UserDetails sharedInstance];
        location = [Location sharedInstance];
        inviteAlerts = [[NSMutableArray alloc] init];
        _goingToJoin = nil;
        _createdToJoin = nil;
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
        
        ChatroomManagement* __weak weakSelf = _sharedObject;
        [[Connection sharedInstance] addCallbackBlock:^(MessageBase* m){
            Connection* c = [Connection sharedInstance];
            dispatch_group_async(c.parseGroup, c.parseQueue, ^{
                [weakSelf messageCallback:m];
            });
        } fromSender:NSStringFromClass([self class])];
    });
    return _sharedObject;
}


#pragma mark - Convenience

- (long long)currentChatroomId
{
    if ([_joinedChatrooms count] > 0) {
        return [[_joinedChatrooms allKeys][0] longLongValue];
    }
    else {
        return 0;
    }
}

- (NSString*)currentChatroomName
{
    if ([_joinedChatrooms count] > 0) {
        return [_joinedChatrooms allValues][0];
    }
    else {
        return @"";
    }
}

- (NSMutableArray*)currentChatQueue
{
    return [_chatQueue allValues][0];
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

- (BOOL)canJoinChatroom:(ChatroomMessage*)chatroom
{
    CLLocationCoordinate2D chatroomOrigin = CLLocationCoordinate2DMake([Location fromLongLong:chatroom.latitude], [Location fromLongLong:chatroom.longitude]);
    return [self canJoinChatroomWithCoord:chatroomOrigin andRadius:chatroom.radius];
}


#pragma mark - Managing chatroom memberships

// Dispatch the sending of the message
- (void)joinChatroomWithId:(long long)chatId latitude:(long long)lat longitude:(long long)lng
{
    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
    msg.userId = ud.userId;
    msg.chatroomId = chatId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
}


#pragma mark - Messages

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case Message:
            NSLog(@"[ChatroomManagement] Message");
            [self receivedMessage:(MessageMessage*)message];
            break;
            
        case JoinedChatroom:
            NSLog(@"[ChatroomManagement] JoinedChatroom");
            [self receivedJoinedChatroom:(JoinedChatroomMessage*)message];
            break;
            
        case LeftChatroom:
            NSLog(@"[ChatroomManagement] LeftChatroom");
            [self receivedLeftChatroom:(LeftChatroomMessage*)message];
            break;
            
        case InviteUser:
        {
            if (ud.receiveInviteNotifications) {
                InviteUserMessage* ium = (InviteUserMessage*)message;
                if ([self canJoinChatroomWithCoord:CLLocationCoordinate2DMake(ium.chatroomLat, ium.chatroomLong) andRadius:ium.chatroomRadius]) {
                    [self performSelectorOnMainThread:@selector(showInviteAlert:) withObject:ium waitUntilDone:NO];
                }
                else {
                    NSLog(@"got invite, but chatroom is too far away");
                }
            }
            break;
        }
    }
}

- (void)showInviteAlert:(InviteUserMessage*)ium
{
    NSString* alertMsg = [NSString stringWithFormat:@"%@ has invite you to chatroom %@",ium.senderHandle,ium.chatroomName];
    NSLog(@"%@",alertMsg);
    UIInviteAlertView* alert = [[UIInviteAlertView alloc] initWithTitle:@"Invitation!" message:alertMsg delegate:self cancelButtonTitle:nil otherButtonTitles:@"Join",@"Decline",nil];
    alert.inviteMessage = ium;
    [inviteAlerts addObject:alert];
    [alert show];
}

- (void)addMessage:(MessageBase*)message toChatroom:(NSNumber*)chatroomId
{
    NSMutableArray* msgList = [_chatQueue objectForKey:chatroomId];
    
    if (msgList) {
        [msgList addObject:message];
        
        if ([msgList count] > _MESSAGE_NUM_THRESH) {
            [msgList removeObjectAtIndex:0];
        }
    }
}

- (void)receivedMessage:(MessageMessage*) message
{
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    [self addMessage:message toChatroom:cid];
}

- (void)receivedJoinedChatroom:(JoinedChatroomMessage*)message
{
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    if (message.userId == ud.userId) {
        NSMutableArray* msgList = [[NSMutableArray alloc] init];
        [_chatQueue setObject:msgList forKey:cid];
        [_joinedChatrooms setObject:message.chatroomName forKey:cid];
    }
    else {
        [self addMessage:message toChatroom:cid];
    }
}

- (void)receivedLeftChatroom:(LeftChatroomMessage*)message
{
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    
    if (message.userId == ud.userId) {
        [_joinedChatrooms removeObjectForKey:cid];
        [_chatQueue removeObjectForKey:cid];
    }
    else {
        [self addMessage:message toChatroom:cid];
    }
}


#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    // 0 == JOIN
    if (0 == buttonIndex) {
        [self setGoingToJoin:((UIInviteAlertView*)alertView).inviteMessage];
        [self dismissAllInviteAlerts];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"InviteNotification" object:self];
    }
}

- (void)dismissAllInviteAlerts
{
    for (UIAlertView* alert in inviteAlerts) {
        [alert dismissWithClickedButtonIndex:1 animated:YES];
    }
}




@end
