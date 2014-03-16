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

@interface ChatroomManagement : NSObject

@property (atomic,strong) NSMutableDictionary* chatrooms;
@property (atomic,strong) NSMutableArray* globalChatrooms;
@property (atomic,strong) NSMutableArray* localChatrooms;
@property (atomic,strong) NSMutableArray* joinedChatrooms;

// Combine these two
@property (atomic, retain) Chatroom* goingToJoin;
@property (atomic, retain) Chatroom* createdToJoin;


+ (id)sharedInstance;
- (BOOL)canJoinChatroom:(Chatroom*)chatroom;
- (BOOL)canJoinChatroomWithCoord:(CLLocationCoordinate2D)coord andRadius:(long long)radius;


// TO BE DEPRECATED
- (Chatroom*)currentChatroom; // NOTE: Assumes one joined chatroom

@end
