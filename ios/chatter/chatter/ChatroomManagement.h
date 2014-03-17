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

@interface ChatroomManagement : NSObject

@property (atomic,strong) NSMutableDictionary* chatrooms;
@property (atomic,strong) NSMutableArray* globalChatrooms;
@property (atomic,strong) NSMutableArray* localChatrooms;
@property (atomic,strong) NSMutableArray* joinedChatrooms;

// Combine these two
//@property (atomic, retain) Chatroom* goingToJoin;
//@property (atomic, retain) Chatroom* createdToJoin;


+ (id)sharedInstance;
- (BOOL)canJoinChatroom:(Chatroom*)chatroom;
- (BOOL)canJoinChatroomWithCoord:(CLLocationCoordinate2D)coord andRadius:(long long)radius;
- (void)searchChatrooms;
- (void)joinChatroom:(Chatroom*)chatroom withCompletion:(JoinCompletion)completion;
- (void)joinChatroomWithId:(NSNumber*)chatroomId withCompletion:(JoinCompletion)completion;
- (BOOL)alreadyJoinedChatroom:(Chatroom*)chatroom;

@end
