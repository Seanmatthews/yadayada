//
//  UserDetails.m
//  chatter
//
//  Created by sean matthews on 10/27/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "UserDetails.h"

@implementation UserDetails


- (id)init
{
    self = [super init];
    
    if (self) {
        _handle = [[NSUserDefaults standardUserDefaults] stringForKey:@"UserHandle"];
        _receiveChatroomNotifications = [[NSUserDefaults standardUserDefaults] boolForKey:@"ReceiveChatNotifications"];
        _receiveMessageNotifications = [[NSUserDefaults standardUserDefaults] boolForKey:@"ReceiveMessageNotifications"];
        
        // TODO: this
        // self = [[NSUserDefaults standardUserDefaults] objectForKey:@"UserDetails"];
        
        // This will be the same while the user has any apps installed with the
        // same com.whatever app identifier, and will change when the user
        // uninstalls all of those apps and then reinstalls another.
        _UUID = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
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

- (id) initWithHandle:(NSString*)handle
{
    self = [super init];
    if (self) {
        _handle = handle;
        _UUID = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        [[NSUserDefaults standardUserDefaults] setObject:_handle forKey:@"UserHandle"];
    }
    return self;
}

- (void)setHandle:(NSString *)handle
{
    if (_handle != handle) {
        _handle = handle;
        [[NSUserDefaults standardUserDefaults] setObject:_handle forKey:@"UserHandle"];
    }
}

@end
