//
//  ChatroomManagement.h
//  chatter
//
//  Created by sean matthews on 2/4/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

@import Foundation;
#import "Messages.h"
#import "UserDetails.h"
#import "Location.h"
#import "Chatroom.h"

typedef void (^JoinCompletion)(void);
typedef void (^LeaveCompletion)(void);
typedef void (^CreateCompletion)(long long);

@interface ChatroomManagement : NSObject

@property (atomic,strong) NSMutableDictionary* chatrooms;
@property (atomic,strong) NSMutableArray* globalChatrooms;
@property (atomic,strong) NSMutableArray* localChatrooms;
@property (atomic,strong) NSMutableArray* joinedChatrooms;
@property (nonatomic,strong) NSNumber* globalChatroomId;


+ (id)sharedInstance;
- (BOOL)canJoinChatroom:(Chatroom*)chatroom;
- (BOOL)canJoinChatroomWithCoord:(CLLocationCoordinate2D)coord andRadius:(long long)radius;
- (void)searchChatrooms;
- (void)joinChatroom:(Chatroom*)chatroom withCompletion:(JoinCompletion)completion;
- (void)joinChatroomWithId:(NSNumber*)chatroomId withCompletion:(JoinCompletion)completion;
- (void)leaveChatroomWithId:(NSNumber*)chatroomId withCompletion:(LeaveCompletion)completion;
- (void)createChatroom:(Chatroom*)chatroom withCompletion:(CreateCompletion)completion;
- (void)leaveJoinedChatrooms;

@end
