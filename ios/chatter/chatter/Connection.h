//
//  Connection.h
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import Foundation;
#import "MessageUtils.h"
#import "UserDetails.h"


@interface Connection : NSObject <NSStreamDelegate>

@property BOOL streamReady;
@property NSCondition* messageCondition;
@property dispatch_group_t parseGroup;
@property dispatch_queue_t parseQueue;

+ (id)sharedInstance;
- (id)init;
- (void)connect;
- (void)reconnect;
- (void)connectToImageServer;
- (void)sendMessage:(MessageBase*)message;
- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url;
- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(id)sender;
- (void)removeCallbackBlockFromSender:(NSString*)sender;
- (int)getImageServerPort;

@end
