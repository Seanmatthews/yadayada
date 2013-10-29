//
//  MessageUtils.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Messages.h"
#import "OrderedDictionary.h"

typedef uint8_t BUFDECLTYPE;
typedef const uint8_t* BUFTYPE;


@interface MessageUtils : NSObject

+ (MessageBase*)messageWithType:(MessageTypes)type;
+ (NSData*)serializeMessage:(MessageBase*)message;
+ (MessageBase*)deserializeMessage:(BUFTYPE)data withLength:(int*)length;
+ (OrderedDictionary *)classPropsFor:(Class)klass;

@end
