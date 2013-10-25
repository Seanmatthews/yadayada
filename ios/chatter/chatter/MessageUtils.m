//
//  MessageUtils.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MessageUtils.h"
#import "objc/runtime.h"

@implementation MessageUtils

+ (NSData*)serializeMessage:(Message*)message
{
    NSData* data;
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList([message class], &outCount);
    for(i = 0; i < outCount; i++) {
        objc_property_t property = properties[i];
        const char *propName = property_getName(property);
        if (propName) {
            NSLog(@"prop name: %s",propName);
//            const char *propType = getPropertyType(property);
//            NSString *propertyName = [NSString stringWithCString:propName
//                                                        encoding:[NSString defaultCStringEncoding]];
//            NSString *propertyType = [NSString stringWithCString:propType
//                                                        encoding:[NSString defaultCStringEncoding]];
        }
    }
    free(properties);
    return data;
}

@end
