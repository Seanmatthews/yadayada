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

@implementation ChatroomManagement

const int MESSAGE_NUM_THRESH = 50;

- (id)init
{
    self = [super init];
    
    if (self) {
        _chatQueue = [[NSMutableDictionary alloc] init];
        _joinedChatrooms = [[NSMutableDictionary alloc] init];
        ud = [UserDetails sharedInstance];
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
    return [[_joinedChatrooms allKeys][0] longLongValue];
}

- (NSString*)currentChatroomName
{
    return [_joinedChatrooms allValues][0];
}

- (NSMutableArray*)currentChatQueue
{
    return [_chatQueue allValues][0];
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
    NSNumber* cid = [NSNumber numberWithLongLong:message.chatroomId];
    
    if (message.userId == ud.userId) {
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


@end
