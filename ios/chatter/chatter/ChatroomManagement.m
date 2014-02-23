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
#import "UIAlertView+InviteAlertView.h"

@implementation ChatroomManagement

const int MESSAGE_NUM_THRESH = 50;

- (id)init
{
    self = [super init];
    
    if (self) {
        _chatQueue = [[NSMutableDictionary alloc] init];
        _joinedChatrooms = [[NSMutableDictionary alloc] init];
        ud = [UserDetails sharedInstance];
        location = [Location sharedInstance];
        inviteAlerts = [[NSMutableArray alloc] init];
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
            InviteUserMessage* ium = (InviteUserMessage*)message;
            NSString* alertMsg = [NSString stringWithFormat:@"%@ has invite you to chatroom %@",ium.senderHandle,ium.chatroomName];
            NSLog(@"%@",alertMsg);
//            UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Invitation!" message:alertMsg delegate:self cancelButtonTitle:nil otherButtonTitles:@"Join",@"Decline",nil];
//            alert.inviteMessage = ium;
//            [inviteAlerts addObject:alert];
//            [alert show];
            break;
        }
    }
}

- (void)receivedMessage:(MessageMessage*) message
{
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    NSMutableArray* msgList = (NSMutableArray*)[_chatQueue objectForKey:cid];

    if (msgList) {
        [msgList addObject:message];
        
        if ([msgList count] > MESSAGE_NUM_THRESH) {
            [msgList removeObjectAtIndex:0];
        }
    }
}

- (void)receivedJoinedChatroom:(JoinedChatroomMessage*)message
{
    if (message.userId == ud.userId) {
        NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
        NSMutableArray* msgList = [[NSMutableArray alloc] init];
        [_chatQueue setObject:msgList forKey:cid];
        [_joinedChatrooms setObject:message.chatroomName forKey:cid];
    }
}

- (void)receivedLeftChatroom:(LeftChatroomMessage*)message
{
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    
    if (message.userId == ud.userId) {
        [_joinedChatrooms removeObjectForKey:cid];
        [_chatQueue removeObjectForKey:cid];
    }
}


#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    // 0 == JOIN
    if (0 == buttonIndex) {
        [self setGoingToJoin:alertView.inviteMessage];
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
