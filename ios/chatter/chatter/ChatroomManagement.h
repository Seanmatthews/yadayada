//
//  ChatroomManagement.h
//  chatter
//
//  Created by sean matthews on 2/4/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Messages.h"
#import "UserDetails.h"

@interface ChatroomManagement : NSObject
{
    UserDetails* ud;
}

- (id)init;
+ (id)sharedInstance;
- (void)messageCallback:(MessageBase*)message;
- (void)receivedMessage:(MessageMessage*) message;
- (void)receivedJoinedChatroom:(JoinedChatroomMessage*)message;
- (void)receivedLeftChatroom:(LeftChatroomMessage*)message;

// To be deprecated
- (long long)currentChatroomId;
- (NSString*)currentChatroomName;
- (NSMutableArray*)currentChatQueue;

@property (atomic, retain) NSMutableDictionary* chatQueue;
@property (atomic, retain) NSMutableDictionary* joinedChatrooms;

// Not yet used
@property (atomic, retain) NSMutableDictionary* peopleInChat;

@end
