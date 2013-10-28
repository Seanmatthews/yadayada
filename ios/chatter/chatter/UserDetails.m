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
        
        // This will be the same while the user has any apps installed with the
        // same com.whatever app identifier, and will change when the user
        // uninstalls all of those apps and then reinstalls another.
        _UUID = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    }
    return self;
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
