//
//  Messages.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Messages.h"

@implementation MessageBase

@synthesize length;
@synthesize type;

- (id)init
{
    _type = 0;
    _length = 1;
    return self;
}

@end


@implementation RegisterMessage

@synthesize userName;
@synthesize password;
@synthesize handle;

- (id)init
{
    self = [super init];
    _type = (unsigned char)1;
    return self;
}

@end

@implementation LoginMessage

@synthesize userName;
@synthesize password;

- (id)init
{
    self = [super init];
    _type = (unsigned char)11;
    return self;
}

@end

@implementation ConnectMessage

@synthesize APIVersion;
@synthesize UUID;

- (id)init
{
    self = [super init];
    _type = (unsigned char)16;
    return self;
}

@end

@implementation SubmitMessageMessage

@synthesize userId;
@synthesize chatroomId;
@synthesize message;

- (id)init
{
    self = [super init];
    _type = (unsigned char)21;
    return self;
}

@end

@implementation SearchChatroomsMessage

@synthesize latitude;
@synthesize longitude;

- (id)init
{
    self = [super init];
    _type = (unsigned char)31;
    return self;
}

@end

@implementation JoinChatroomMessage

@synthesize userId;
@synthesize chatroomId;
@synthesize latitude;
@synthesize longitude;

- (id)init
{
    self = [super init];
    _type = (unsigned char)33;
    return self;
}

@end

@implementation LeaveChatroomMessage

@synthesize userId;
@synthesize chatroomId;

- (id)init
{
    self = [super init];
    _type = (unsigned char)34;
    return self;
}

@end

@implementation CreateChatroomMessage

@synthesize ownerId;
@synthesize chatroomName;
@synthesize latitude;
@synthesize longitude;
@synthesize radius;

- (id)init
{
    self = [super init];
    _type = (unsigned char)35;
    return self;
}

@end

@implementation RegisterAcceptMessage

@synthesize userId;

- (id)init
{
    self = [super init];
    _type = (unsigned char)2;
    return self;
}

@end

@implementation RegisterRejectMessage

@synthesize reason;

- (id)init
{
    self = [super init];
    _type = (unsigned char)3;
    return self;
}

@end

@implementation LoginAcceptMessage

@synthesize userId;

- (id)init
{
    self = [super init];
    _type = (unsigned char)12;
    return self;
}

@end

@implementation LoginRejectMessage

@synthesize reason;

- (id)init
{
    self = [super init];
    _type = (unsigned char)13;
    return self;
}

@end

@implementation ConnectAcceptMessage

@synthesize APIVersion;
@synthesize globalChatId;

- (id)init
{
    self = [super init];
    _type = (unsigned char)17;
    return self;
}

@end

@implementation ConnectRejectMessage

@synthesize reason;

- (id)init
{
    self = [super init];
    _type = (unsigned char)18;
    return self;
}

@end

@implementation MessageMessage

@synthesize messageId;
@synthesize messageTimestamp;
@synthesize senderId;
@synthesize chatroomId;
@synthesize senderHandle;
@synthesize message;

- (id)init
{
    self = [super init];
    _type = (unsigned char)22;
    return self;
}

@end

@implementation ChatroomMessage

@synthesize chatroomId;
@synthesize chatroomOwnerId;
@synthesize chatroomName;
@synthesize chatroomOwnerHandle;
@synthesize latitude;
@synthesize longitude;
@synthesize radius;

- (id)init
{
    self = [super init];
    _type = (unsigned char)32;
    return self;
}

@end

@implementation JoinChatroomRejectMessage

@synthesize chatroomId;
@synthesize reason;

- (id)init
{
    self = [super init];
    _type = (unsigned char)36;
    return self;
}

@end

@implementation JoinedChatroomMessage

@synthesize chatroomId;
@synthesize userId;
@synthesize userHandle;

- (id)init
{
    self = [super init];
    _type = (unsigned char)37;
    return self;
}

@end

@implementation LeftChatroomMessage

@synthesize chatroomId;
@synthesize userId;

- (id)init
{
    self = [super init];
    _type = (unsigned char)38;
    return self;
}

@end

@implementation CreateChatroomRejectMessage

@synthesize chatroomName;
@synthesize reason;

- (id)init
{
    self = [super init];
    _type = (unsigned char)39;
    return self;
}

@end
