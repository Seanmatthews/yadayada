//
//  Messages.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Messages.h"

@implementation MessageBase

- (id)init
{
    _type = 0;
    _length = 1;
    return self;
}

@end


@implementation RegisterMessage

- (id)init
{
    self = [super init];
    _type = 1;
    return self;
}

@end

@implementation RegisterAcceptMessage

- (id)init
{
    self = [super init];
    _type = 2;
    return self;
}

@end

@implementation RegisterRejectMessage

- (id)init
{
    self = [super init];
    _type = 3;
    return self;
}

@end

@implementation LoginMessage

- (id)init
{
    self = [super init];
    _type = 11;
    return self;
}

@end

@implementation LoginAcceptMessage

- (id)init
{
    self = [super init];
    _type = 12;
    return self;
}

@end

@implementation LoginRejectMessage

- (id)init
{
    self = [super init];
    _type = 13;
    return self;
}

@end

@implementation QuickLoginMessage

- (id)init
{
    self = [super init];
    _type = 14;
    return self;
}

@end

@implementation ConnectMessage

- (id)init
{
    self = [super init];
    _type = 16;
    return self;
}

@end

@implementation ConnectAcceptMessage

- (id)init
{
    self = [super init];
    _type = 17;
    return self;
}

@end

@implementation ConnectRejectMessage

- (id)init
{
    self = [super init];
    _type = 18;
    return self;
}

@end

@implementation HeartbeatMessage

- (id)init
{
    self = [super init];
    _type = 19;
    return self;
}

@end

@implementation SubmitMessageMessage

- (id)init
{
    self = [super init];
    _type = 21;
    return self;
}

@end

@implementation MessageMessage

- (id)init
{
    self = [super init];
    _type = 22;
    return self;
}

@end

@implementation SubmitMessageRejectMessage

- (id)init
{
    self = [super init];
    _type = 23;
    return self;
}

@end

@implementation SearchChatroomsMessage

- (id)init
{
    self = [super init];
    _type = 31;
    return self;
}

@end

@implementation ChatroomMessage

- (id)init
{
    self = [super init];
    _type = 32;
    return self;
}

@end

@implementation JoinChatroomMessage

- (id)init
{
    self = [super init];
    _type = 33;
    return self;
}

@end

@implementation LeaveChatroomMessage

- (id)init
{
    self = [super init];
    _type = 34;
    return self;
}

@end

@implementation CreateChatroomMessage

- (id)init
{
    self = [super init];
    _type = 35;
    return self;
}

@end

@implementation JoinChatroomRejectMessage

- (id)init
{
    self = [super init];
    _type = 36;
    return self;
}

@end

@implementation JoinedChatroomMessage

- (id)init
{
    self = [super init];
    _type = 37;
    return self;
}

@end

@implementation LeftChatroomMessage

- (id)init
{
    self = [super init];
    _type = 38;
    return self;
}

@end

@implementation CreateChatroomRejectMessage

- (id)init
{
    self = [super init];
    _type = 39;
    return self;
}

@end

@implementation VoteMessage

- (id)init
{
    self = [super init];
    _type = 44;
    return self;
}

@end
