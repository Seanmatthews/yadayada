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

@interface MessageUtils : NSObject

+ (NSData*)serializeMessage:(MessageBase*)message;
+ (OrderedDictionary *)classPropsFor:(Class)klass;

@end
