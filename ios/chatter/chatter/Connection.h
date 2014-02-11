//
//  Connection.h
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MessageUtils.h"
#import "UserDetails.h"


@interface Connection : NSObject <NSStreamDelegate>
{
    NSInputStream* is;
    NSOutputStream* os;
    NSInputStream* imgIs;
    NSOutputStream* imgOs;
    NSMutableDictionary* controllers;
    BUFDECLTYPE internalBuffer[8096];
    int internalBufferLen;
}

@property BOOL streamReady;
@property NSCondition* messageCondition;
@property dispatch_group_t parseGroup;
@property dispatch_queue_t parseQueue;


+ (id)sharedInstance;
- (id)init;
- (void)connect;
- (void)connectToImageServer;
- (void)sendMessage:(MessageBase*)message;
- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url;
- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length;
- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(id)sender;
- (void)removeCallbackBlockFromSender:(NSString*)sender;
- (int)getImageServerPort;

@end
