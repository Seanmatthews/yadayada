//
//  Chatroom.m
//  chatter
//
//  Created by sean matthews on 3/7/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "Chatroom.h"
#import "Location.h"

@implementation Chatroom

- (id)init
{
    self = [super init];
    if (self) {
        _MESSAGE_NUM_THRESH = 50;
        _chatQueue = [[NSMutableArray alloc] init];
        _members = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (id)initWithChatroomMessage:(ChatroomMessage *)message
{
    self = [super init];
    if (self) {
        _MESSAGE_NUM_THRESH = 50;
        _cid = [NSNumber numberWithLongLong:message.chatroomId];
        _chatroomName = message.chatroomName;
        _chatQueue = [[NSMutableArray alloc] init];
        _members = [[NSMutableDictionary alloc] init];
        _origin = CLLocationCoordinate2DMake([Location fromLongLong:message.latitude],
                                             [Location fromLongLong:message.longitude]);
        _radius = [NSNumber numberWithLongLong:message.radius];
        _chatroomOwnerId = [NSNumber numberWithLongLong:message.chatroomOwnerId];
        _chatroomOwnerHandle = message.chatroomOwnerHandle;
        _userCount = [NSNumber numberWithInt:message.userCount];
        _chatActivity = [NSNumber numberWithShort:message.chatActivity];
        _exclusive = message.isPrivate > 0;
        _global = message.radius <= 0;
    }
    return self;
}

+ (Chatroom*)chatroomWithChatroomMessage:(ChatroomMessage*)message
{
    return [[Chatroom alloc] initWithChatroomMessage:message];
}

//- (void)insertObject:(id)message inChatQueueAtIndex:(NSUInteger)index
//{
//    [self.chatQueue insertObject:message atIndex:index];
//    return;
//}
//
//- (void)removeObjectFromChatQueueAtIndex:(NSUInteger)index
//{
//    [self.chatQueue removeObjectAtIndex:index];
//}
//
//- (void)replaceObjectInChatQueueAtIndex:(NSUInteger)index withObject:(id)anObject
//{
//    
//    [self.chatQueue replaceObjectAtIndex:index withObject:anObject];
//}



@end
