//
//  Connection.h
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MessageUtils.h"

@interface Connection : NSObject <NSStreamDelegate>
{
    NSInputStream* is;
    NSOutputStream* os;
    NSMutableDictionary* controllers;
}

- (id)init;
- (void)sendMessage:(MessageBase*)message;
- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length;
- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(id)sender;
- (void)removeCallbackBlockFromSender:(id)sender;

@end
