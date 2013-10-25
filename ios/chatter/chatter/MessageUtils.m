//
//  MessageUtils.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MessageUtils.h"
#import "objc/runtime.h"
#import "Messages.h"


@implementation MessageUtils

+ (NSData*)serializeMessage:(MessageBase*)message
{
    NSMutableData* data = [[NSMutableData alloc] init];
    OrderedDictionary* props = [self classPropsFor:[message class]];
    short msgLen = 1;
    
    // going to replace this
    [data appendBytes:&msgLen length:2];
    Byte b = message.type;
    [data appendBytes:&b length:1];
    
    for (NSString* key in props) {
        NSString* typename = [props valueForKey:key];
        NSLog(@"%@ : %@",key,typename);
        //Class C = NSClassFromString([props valueForKey:key]);
        
        if ([typename isEqualToString:@"NSString"]) {
            short strLen = (short)[[message valueForKey:key] lengthOfBytesUsingEncoding:STRENC];
            strLen = CFSwapInt16HostToBig(strLen);
            [data appendBytes:&strLen length:2];
            [data appendData:[[message valueForKey:key] dataUsingEncoding:STRENC]];
            msgLen += 2 + strLen;
        }
        else if ([typename isEqualToString:@"long long"]) {
            long long l = CFSwapInt64HostToBig((long long)[message valueForKey:key]);
            [data appendBytes:&l length:8];
            msgLen += 8;
        }
        else if ([typename isEqualToString:@"short"]) {
            short s = CFSwapInt16HostToBig((short)[message valueForKey:key]);
            [data appendBytes:&s length:2];
            msgLen += 2;
        }
        else if ([typename isEqualToString:@"int"]) {
            int i = CFSwapInt32HostToBig((int)[message valueForKey:key]);
            [data appendBytes:&i length:4];
            msgLen += 4;
        }
        else if ([typename isEqualToString:@"Byte"]) {
            Byte b = (Byte)[message valueForKey:key];
            [data appendBytes:&b length:1];
            msgLen += 1;
        }
    }
    
    // set the correct msg length
    msgLen = CFSwapInt16HostToBig(msgLen);
    [data replaceBytesInRange:NSMakeRange(0, 2) withBytes:&msgLen length:2];

    return data;
}

//+ (NSData*)getData:(long long)

static const char *getPropertyType(objc_property_t property) {
    const char *attributes = property_getAttributes(property);
    //printf("attributes=%s\n", attributes);
    char buffer[1 + strlen(attributes)];
    strcpy(buffer, attributes);
    char *state = buffer, *attribute;
    while ((attribute = strsep(&state, ",")) != NULL) {
        if (attribute[0] == 'T' && attribute[1] != '@') {
            // it's a C primitive type:
            /*
             if you want a list of what will be returned for these primitives, search online for
             "objective-c" "Property Attribute Description Examples"
             apple docs list plenty of examples of what you get for int "i", long "l", unsigned "I", struct, etc.
             */
            NSString *name = [[NSString alloc] initWithBytes:attribute + 1 length:strlen(attribute) - 1 encoding:NSASCIIStringEncoding];
            return (const char *)[name cStringUsingEncoding:NSASCIIStringEncoding];
        }
        else if (attribute[0] == 'T' && attribute[1] == '@' && strlen(attribute) == 2) {
            // it's an ObjC id type:
            return "id";
        }
        else if (attribute[0] == 'T' && attribute[1] == '@') {
            // it's another ObjC object type:
            NSString *name = [[NSString alloc] initWithBytes:attribute + 3 length:strlen(attribute) - 4 encoding:NSASCIIStringEncoding];
            return (const char *)[name cStringUsingEncoding:NSASCIIStringEncoding];
        }
    }
    return "";
}


+ (OrderedDictionary *)classPropsFor:(Class)klass
{
    if (klass == NULL) {
        return nil;
    }
    
    OrderedDictionary *results = [[OrderedDictionary alloc] init];
    
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList(klass, &outCount);
    for (i = 0; i < outCount; i++) {
        objc_property_t property = properties[i];
        const char *propName = property_getName(property);
        if(propName) {
            const char *propType = getPropertyType(property);
            NSString *propertyName = [NSString stringWithUTF8String:propName];
            NSString *propertyType = [NSString stringWithUTF8String:propType];
            [results setObject:propertyType forKey:propertyName];
        }
    }
    free(properties);
    
    // returning a copy here to make sure the dictionary is immutable
    return [OrderedDictionary dictionaryWithDictionary:results];
}

@end
