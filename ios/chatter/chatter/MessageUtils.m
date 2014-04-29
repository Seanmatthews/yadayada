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

const NSStringEncoding STRENC = NSUTF8StringEncoding;

@implementation MessageUtils

+ (MessageBase*)messageWithType:(MessageTypes)type
{
    MessageBase* mb;

    switch (type) {
        case Register:
        mb = [[RegisterMessage alloc] init];
        break;

        case RegisterAccept:
        mb = [[RegisterAcceptMessage alloc] init];
        break;

        case RegisterReject:
        mb = [[RegisterRejectMessage alloc] init];
        break;

        case Login:
        mb = [[LoginMessage alloc] init];
        break;

        case LoginAccept:
        mb = [[LoginAcceptMessage alloc] init];
        break;

        case LoginReject:
        mb = [[LoginRejectMessage alloc] init];
        break;

        case QuickLogin:
        mb = [[QuickLoginMessage alloc] init];
        break;

        case Connect:
        mb = [[ConnectMessage alloc] init];
        break;

        case ConnectAccept:
        mb = [[ConnectAcceptMessage alloc] init];
        break;

        case ConnectReject:
        mb = [[ConnectRejectMessage alloc] init];
        break;

        case Heartbeat:
        mb = [[HeartbeatMessage alloc] init];
        break;

        case SubmitMessage:
        mb = [[SubmitMessageMessage alloc] init];
        break;

        case Message:
        mb = [[MessageMessage alloc] init];
        break;

        case SubmitMessageReject:
        mb = [[SubmitMessageRejectMessage alloc] init];
        break;

        case SearchChatrooms:
        mb = [[SearchChatroomsMessage alloc] init];
        break;

        case aChatroom:
        mb = [[ChatroomMessage alloc] init];
        break;

        case JoinChatroom:
        mb = [[JoinChatroomMessage alloc] init];
        break;

        case LeaveChatroom:
        mb = [[LeaveChatroomMessage alloc] init];
        break;

        case CreateChatroom:
        mb = [[CreateChatroomMessage alloc] init];
        break;

        case JoinChatroomReject:
        mb = [[JoinChatroomRejectMessage alloc] init];
        break;

        case JoinedChatroom:
        mb = [[JoinedChatroomMessage alloc] init];
        break;

        case LeftChatroom:
        mb = [[LeftChatroomMessage alloc] init];
        break;

        case CreateChatroomReject:
        mb = [[CreateChatroomRejectMessage alloc] init];
        break;

        case Vote:
        mb = [[VoteMessage alloc] init];
        break;

        case InviteUser:
        mb = [[InviteUserMessage alloc] init];
        break;

        case InviteUserReject:
        mb = [[InviteUserRejectMessage alloc] init];
        break;

        case InviteUserSuccess:
        mb = [[InviteUserSuccessMessage alloc] init];
        break;

        case StreamReset:
        mb = [[StreamResetMessage alloc] init];
        break;

        case Terminate:
        mb = [[TerminateMessage alloc] init];
        break;

    }
    return mb;
}

+ (NSData*)serializeMessage:(MessageBase*)message
{
    NSMutableData* data = [[NSMutableData alloc] init];
    short msgLen = 1;
    
    // NOTE: This will only get properties for the subclass passed to the function
    OrderedDictionary* props = [self classPropsFor:[message class]];
    
    // going to replace this
    [data appendBytes:&msgLen length:2];
    Byte b = message.type;
    [data appendBytes:&b length:1];
    
    for (NSString* key in props) {
        NSString* typename = [props valueForKey:key];
        //Class C = NSClassFromString([props valueForKey:key]);
        
        if ([typename isEqualToString:@"NSString"]) {
            short strLen = (short)[[message valueForKey:key] lengthOfBytesUsingEncoding:STRENC];
            short newStrLen = CFSwapInt16HostToBig(strLen);
            [data appendBytes:&newStrLen length:2];
            [data appendData:[[message valueForKey:key] dataUsingEncoding:STRENC]];
            msgLen += 2 + strLen;
        }
        else if ([typename isEqualToString:@"q"]) {
            long long l = CFSwapInt64HostToBig([[message valueForKey:key] longLongValue]);
            [data appendBytes:&l length:8];
            msgLen += 8;
        }
        else if ([typename isEqualToString:@"s"]) {
            short s = CFSwapInt16HostToBig([[message valueForKey:key] shortValue]);
            [data appendBytes:&s length:2];
            msgLen += 2;
        }
        else if ([typename isEqualToString:@"i"]) {
            int i = CFSwapInt32HostToBig([[message valueForKey:key] integerValue]);
            [data appendBytes:&i length:4];
            msgLen += 4;
        }
        else if ([typename isEqualToString:@"C"]) {
            Byte b = (Byte)[[message valueForKey:key] unsignedCharValue];
            [data appendBytes:&b length:1];
            msgLen += 1;
        }
        else {
            NSLog(@"Unrecognized serialization type");
        }
    }
    
    // set the correct msg length
    short newMsgLen = CFSwapInt16HostToBig(msgLen);
    [data replaceBytesInRange:NSMakeRange(0, 2) withBytes:&newMsgLen length:2];

    return data;
}

+ (MessageBase*)deserializeMessage:(BUFTYPE)data withLength:(short)length
{
    // If this is the case, the bytes can't possibly make a message
    if (length < 3) {
        return nil;
    }
    
    MessageBase* mb;
    
    // Header info
    int idx = 0;
    Byte type = *(Byte*)&data[idx];
    idx++;
    mb = [self messageWithType:type];
    
    // NOTE: This will only get properties for the subclass passed to the function
    OrderedDictionary* props = [self classPropsFor:[mb class]];
    
    for (NSString* key in props) {
        
        NSString* typename = [props valueForKey:key];
        
        if ([typename isEqualToString:@"NSString"]) {
            short strLen = CFSwapInt16BigToHost(*(short*)(data+idx));
            idx += 2;
            NSString* str = [[NSString alloc] initWithBytes:(data+idx) length:strLen encoding:STRENC];
            idx += strLen;
            [mb setValue:str forKey:key];
        }
        else if ([typename isEqualToString:@"q"]) {
            long long l = CFSwapInt64BigToHost(*(long long*)&data[idx]);
            idx += 8;
            [mb setValue:[NSNumber numberWithLongLong:l] forKey:key];
        }
        else if ([typename isEqualToString:@"s"]) {
            short s = CFSwapInt16BigToHost(*(short*)&data[idx]);
            idx += 2;
            [mb setValue:[NSNumber numberWithShort:s] forKey:key];
        }
        else if ([typename isEqualToString:@"i"]) {
            int i = CFSwapInt32BigToHost(*(short*)&data[idx]);
            idx += 4;
            [mb setValue:[NSNumber numberWithInt:i] forKey:key];
        }
        else if ([typename isEqualToString:@"C"]) {
            unsigned char b = *(unsigned char*)&data[idx];
            idx++;
            [mb setValue:[NSNumber numberWithUnsignedChar:b] forKey:key];
        }
    }
    
    return mb;
}



static const char *getPropertyType(objc_property_t property) {
    const char *attributes = property_getAttributes(property);
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


+ (MessageBase*)messageWithPushNotification:(NSDictionary*)notification
{
    MessageBase* m = [MessageUtils messageWithType:(MessageTypes)notification[@"message"][@"messageType"]];
    OrderedDictionary* props = [self classPropsFor:[m class]];
    for (NSString* key in props) {
        [m setValue:notification[@"message"][key] forKey:key];
    }
    return m;
}

@end
