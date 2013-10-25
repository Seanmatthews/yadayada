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
    Register = 1),
    Login = 11),
    Connect = 16),
    SubmitMessage = 21),
    SearchChatrooms = 31),
    JoinChatroom = 33),
    LeaveChatroom = 34),
    CreateChatroom = 35),
    RegisterAccept = 2),
    RegisterReject = 3),
    LoginAccept = 12),
    LoginReject = 13),
    ConnectAccept = 17),
    ConnectReject = 18),
    Message = 22),
    Chatroom = 32),
    JoinChatroomReject = 36),
    JoinedChatroom = 37),
    LeftChatroom = 38),
    CreateChatroomReject = 38),
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

- (id)init;

@property NSString* userName;
@property NSString* password;
@property NSString* handle;

@end

@interface LoginMessage : Message

- (id)init;

@property NSString* userName;
@property NSString* password;

@end

@interface ConnectMessage : Message

- (id)init;

@property int APIVersion;
@property NSString* UUID;

@end

@interface SubmitMessageMessage : Message

- (id)init;

@property long long userId;
@property long long chatroomId;
@property NSString* message;

@end

@interface SearchChatroomsMessage : Message

- (id)init;

@property long long latitude;
@property long long longitude;

@end

@interface JoinChatroomMessage : Message

- (id)init;

@property long long userId;
@property long long chatroomId;
@property long long latitude;
@property long long longitude;

@end

@interface LeaveChatroomMessage : Message

- (id)init;

@property long long userId;
@property long long chatroomId;

@end

@interface CreateChatroomMessage : Message

- (id)init;

@property long long ownerId;
@property NSString* chatroomName;
@property long long latitude;
@property long long longitude;
@property long long radius;

@end

@interface RegisterAcceptMessage : Message

- (id)init;

@property long long userId;

@end

@interface RegisterRejectMessage : Message

- (id)init;

@property NSString* reason;

@end

@interface LoginAcceptMessage : Message

- (id)init;

@property long long userId;

@end

@interface LoginRejectMessage : Message

- (id)init;

@property NSString* reason;

@end

@interface ConnectAcceptMessage : Message

- (id)init;

@property int APIVersion;
@property long long globalChatId;

@end

@interface ConnectRejectMessage : Message

- (id)init;

@property NSString* reason;

@end

@interface MessageMessage : Message

- (id)init;

@property long long messageId;
@property long long messageTimestamp;
@property long long senderId;
@property long long chatroomId;
@property NSString* senderHandle;
@property NSString* message;

@end

@interface ChatroomMessage : Message

- (id)init;

@property long long chatroomId;
@property long long chatroomOwnerId;
@property NSString* chatroomName;
@property NSString* chatroomOwnerHandle;
@property long long latitude;
@property long long longitude;
@property long long radius;

@end

@interface JoinChatroomRejectMessage : Message

- (id)init;

@property long long chatroomId;
@property NSString* reason;

@end

@interface JoinedChatroomMessage : Message

- (id)init;

@property long long chatroomId;
@property long long userId;
@property NSString* userHandle;

@end

@interface LeftChatroomMessage : Message

- (id)init;

@property long long chatroomId;
@property long long userId;

@end

@interface CreateChatroomRejectMessage : Message

- (id)init;

@property NSString* chatroomName;
@property NSString* reason;

@end
