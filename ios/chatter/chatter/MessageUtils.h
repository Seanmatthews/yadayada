//
//  MessageUtils.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Messages.h"

@interface MessageUtils : NSObject

+ (NSData*)serializeMessage:(Message*)message;

@end
