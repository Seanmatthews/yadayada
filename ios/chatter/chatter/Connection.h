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
    NSMutableDictionary* controllers;
    BUFDECLTYPE internalBuffer[8096];
    int internalBufferLen;
}

@property BOOL streamReady;

+ (id)sharedInstance;
- (id)init;
- (void)connect;
- (void)sendMessage:(MessageBase*)message;
- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length;
- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(id)sender;
- (void)removeCallbackBlockFromSender:(NSString*)sender;

@end
