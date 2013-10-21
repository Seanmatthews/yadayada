//
//  MessageTypes.h
//  chattertest
//
//  Created by sean matthews on 10/19/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#ifndef chattertest_MessageTypes_h
#define chattertest_MessageTypes_h

typedef enum {
    REGISTER = 1,
    REGISTER_ACCEPT = 2,
    REGISTER_REJECT = 3,
    QUICK_REGISTER = 4,
    LOGIN = 11,
    LOGIN_ACCEPT = 12,
    LOGIN_REJECT = 13,
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

#endif
