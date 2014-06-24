//
//  SnarkyErrorMessages.m
//  chatter
//
//  Created by sean matthews on 6/24/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "SnarkyErrorMessages.h"

@interface SnarkyErrorMessages()
{
    NSArray *badConnectionMessages;
    NSArray *inviteSelfMessages;
}

- (void)initializeArrays;

@end

@implementation SnarkyErrorMessages

- (id)init
{
    self = [super init];
    if (self) {
        [self initializeArrays];
    }
    return self;
}

+ (id)sharedInstance
{
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
        // Additional initialization can go here
    });
    return _sharedObject;
}

- (void)initializeArrays
{
    badConnectionMessages = @[@"I feel like we're just not communicating.",
                              @"Can you speak up?",
                              @"It's not me, it's you.",
                              @"I think you know what the problem is just as well as I do.",
                              @"Daisy, Daisy, give me your answer do."];
    
    inviteSelfMessages    = @[@"Invite yourself? Come on!",
                              @"Talking to yourself again, eh?",
                              @"I don't think this chatroom needs another you.",
                              @"You're your own best friend.",
                              @"Try inviting someone else."];
}

- (NSString*)messageForConnectionError
{
    return [NSString stringWithFormat:@"%@\nBad connection",
            badConnectionMessages[arc4random() % badConnectionMessages.count]];
}

- (NSString*)messageForInviteSelf
{
    return [NSString stringWithFormat:@"%@",
            inviteSelfMessages[arc4random() % inviteSelfMessages.count]];
}

@end
