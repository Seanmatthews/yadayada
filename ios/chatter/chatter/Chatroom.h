//
//  Chatroom.h
//  chatter
//
//  Created by sean matthews on 3/7/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

@import Foundation;
@import CoreLocation;
#import "Messages.h"

@interface ChatMessage : NSObject



@end

@interface Chatroom : NSObject

@property (nonatomic,strong) NSNumber* cid;
@property (nonatomic,strong) NSString* chatroomName;
@property (atomic,strong) NSMutableArray* chatQueue;
@property (nonatomic,strong) NSMutableDictionary* members;
@property (nonatomic) CLLocationCoordinate2D origin;
@property (nonatomic,strong) NSNumber* radius;
@property (nonatomic,strong) NSNumber* chatroomOwnerId;
@property (nonatomic,strong) NSString* chatroomOwnerHandle;
@property (nonatomic,strong) NSNumber* userCount;
@property (nonatomic,strong) NSNumber* chatActivity;
@property (nonatomic,getter=isPrivate,setter=setPrivate:) BOOL exclusive;
@property (nonatomic,getter=isGlobal) BOOL global;

- (id)init;
- (id)initWithChatroomMessage:(ChatroomMessage*)message;
//- (id)initWithJoinedChatroomMessage:(JoinedChatroomMessage*)message;
+ (Chatroom*)chatroomWithChatroomMessage:(ChatroomMessage*)message;
//+ (Chatroom*)chatroomWithJoinedChatroomMessage:(JoinedChatroomMessage*)message;

@end
