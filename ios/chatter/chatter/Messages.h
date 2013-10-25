//
//  Messages.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MessageTemplate.h"

typedef enum {
    REGISTER = 1,
    REGISTER_ACCEPT = 2,
    REGISTER_REJECT = 3,
    QUICK_REGISTER = 4,
    LOGIN = 11,
    LOGIN_ACCEPT = 12,
    LOGIN_REJECT = 13,
    CONNECT = 16,
    CONNECT_ACCEPT = 17,
    CONNECT_REJECT = 18,
    SUBMIT_MESSAGE = 21,
    MESSAGE = 22,
    SEARCH_CHATROOMS = 31,
    CHATROOM = 32,
    JOIN_CHATROOM = 33,
    LEAVE_CHATROOM = 34,
    CREATE_CHATROOM = 35,
    JOIN_CHATROOM_FAILURE = 36,
    JOINED_CHATROOM = 37,
    LEFT_CHATROOM = 38
} MessageTypes;

@interface Message : NSObject
{
    @protected
    short _length;
    
    @protected
    Byte _type;
}

@property (readonly) short length;
@property (readonly) Byte type;

@end


@interface RegisterMessage : Message

@property NSString* username;
@property NSString* password;
@property NSString* handle;

@end


@interface RegisterAcceptMessage : Message

@property long long userId;

@end


@interface RegisterRejectMessage : Message

@property NSString* message;

@end
