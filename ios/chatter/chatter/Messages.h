//
//  Messages.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum {
    Register = 1,
    RegisterAccept = 2,
    RegisterReject = 3,
    Login = 11,
    LoginAccept = 12,
    LoginReject = 13,
    QuickLogin = 14,
    Connect = 16,
    ConnectAccept = 17,
    ConnectReject = 18,
    Heartbeat = 19,
    SubmitMessage = 21,
    Message = 22,
    SubmitMessageReject = 23,
    SearchChatrooms = 31,
    aChatroom = 32,
    JoinChatroom = 33,
    LeaveChatroom = 34,
    CreateChatroom = 35,
    JoinChatroomReject = 36,
    JoinedChatroom = 37,
    LeftChatroom = 38,
    CreateChatroomReject = 39,
    Vote = 44,
    InviteUser = 50,
    InviteUserReject = 51,
    InviteUserSuccess = 52,
    StreamReset = 60,
} MessageTypes;

@interface MessageBase : NSObject
{
    @protected
    short _length;

    @protected
    Byte _type;
}

@property (readonly) short length;
@property (readonly) Byte type;
@end


@interface RegisterMessage : MessageBase

- (id)init;

@property NSString* userName;
@property NSString* password;
@property NSString* handle;
@property NSString* UUID;
@property long long phoneNumber;

@end

@interface RegisterAcceptMessage : MessageBase

- (id)init;

@property long long userId;

@end

@interface RegisterRejectMessage : MessageBase

- (id)init;

@property NSString* reason;

@end

@interface LoginMessage : MessageBase

- (id)init;

@property NSString* userName;
@property NSString* password;

@end

@interface LoginAcceptMessage : MessageBase

- (id)init;

@property long long userId;

@end

@interface LoginRejectMessage : MessageBase

- (id)init;

@property NSString* reason;

@end

@interface QuickLoginMessage : MessageBase

- (id)init;

@property NSString* handle;
@property NSString* UUID;
@property long long phoneNumber;
@property NSString* deviceToken;

@end

@interface ConnectMessage : MessageBase

- (id)init;

@property int APIVersion;
@property NSString* UUID;
@property NSString* deviceToken;

@end

@interface ConnectAcceptMessage : MessageBase

- (id)init;

@property int APIVersion;
@property long long globalChatId;
@property NSString* imageUploadUrl;
@property NSString* imageDownloadUrl;
@property short heartbeatInterval;

@end

@interface ConnectRejectMessage : MessageBase

- (id)init;

@property NSString* reason;

@end

@interface HeartbeatMessage : MessageBase

- (id)init;

@property long long timestamp;
@property long long latitude;
@property long long longitude;

@end

@interface SubmitMessageMessage : MessageBase

- (id)init;

@property long long userId;
@property long long chatroomId;
@property NSString* message;

@end

@interface MessageMessage : MessageBase

- (id)init;

@property long long messageId;
@property long long messageTimestamp;
@property long long senderId;
@property long long chatroomId;
@property NSString* senderHandle;
@property NSString* message;

@end

@interface SubmitMessageRejectMessage : MessageBase

- (id)init;

@property long long userId;
@property long long chatroomId;
@property NSString* reason;

@end

@interface SearchChatroomsMessage : MessageBase

- (id)init;

@property long long latitude;
@property long long longitude;
@property Byte onlyJoinable;
@property long long metersFromCoords;

@end

@interface ChatroomMessage : MessageBase

- (id)init;

@property long long chatroomId;
@property long long chatroomOwnerId;
@property NSString* chatroomName;
@property NSString* chatroomOwnerHandle;
@property long long latitude;
@property long long longitude;
@property long long radius;
@property int userCount;
@property short chatActivity;
@property Byte isPrivate;

@end

@interface JoinChatroomMessage : MessageBase

- (id)init;

@property long long userId;
@property long long chatroomId;
@property long long latitude;
@property long long longitude;

@end

@interface LeaveChatroomMessage : MessageBase

- (id)init;

@property long long userId;
@property long long chatroomId;

@end

@interface CreateChatroomMessage : MessageBase

- (id)init;

@property long long ownerId;
@property NSString* chatroomName;
@property long long latitude;
@property long long longitude;
@property long long radius;
@property Byte isPrivate;

@end

@interface JoinChatroomRejectMessage : MessageBase

- (id)init;

@property long long chatroomId;
@property NSString* reason;

@end

@interface JoinedChatroomMessage : MessageBase

- (id)init;

@property long long userId;
@property NSString* userHandle;
@property long long chatroomId;
@property long long chatroomOwnerId;
@property NSString* chatroomName;
@property NSString* chatroomOwnerHandle;
@property long long latitude;
@property long long longitude;
@property long long radius;
@property int userCount;
@property short chatActivity;

@end

@interface LeftChatroomMessage : MessageBase

- (id)init;

@property long long chatroomId;
@property long long userId;
@property NSString* userHandle;

@end

@interface CreateChatroomRejectMessage : MessageBase

- (id)init;

@property NSString* chatroomName;
@property NSString* reason;

@end

@interface VoteMessage : MessageBase

- (id)init;

@property long long voterId;
@property long long votedId;
@property long long msgId;
@property long long chatroomId;
@property Byte upvote;

@end

@interface InviteUserMessage : MessageBase

- (id)init;

@property long long senderId;
@property NSString* senderHandle;
@property long long recipientId;
@property long long chatroomId;
@property NSString* chatroomName;
@property long long chatroomLat;
@property long long chatroomLong;
@property long long chatroomRadius;
@property long long recipientPhoneNumber;

@end

@interface InviteUserRejectMessage : MessageBase

- (id)init;

@property NSString* reason;

@end

@interface InviteUserSuccessMessage : MessageBase

- (id)init;

@property long long inviteeUserId;
@property NSString* inviteeHandle;
@property NSString* chatroomName;

@end

@interface StreamResetMessage : MessageBase

- (id)init;

@property long long userId;

@end
