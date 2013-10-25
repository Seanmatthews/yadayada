//
//  Messages.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Messages.h"


@implementation Message

@synthesize length;
@synthesize type;

- (id)init
{
    _type = 0;
    return self;
}

@end


@implementation RegisterMessage

- (id)init
{
    self = [super init];
    _type = REGISTER;
    return self;
}

@end


