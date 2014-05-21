//
//  CurrentViewNotifier.m
//  chatter
//
//  Created by sean matthews on 5/20/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "CurrentViewNotifier.h"

@interface CurrentViewNotifier ()
{
    NSString* currentViewClassName;
}

- (void)setViewDidAppear:(NSNotification*)notification;

@end


@implementation CurrentViewNotifier

- (id)init
{
    self = [super init];
    if (self) {
        currentViewClassName = nil;
    }
    return self;
}

+ (id)sharedNotifier
{
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
        // Additional initialization can go here
//        [_sharedObject registerForNotifications];
    });
    return _sharedObject;
}

- (void)registerForNotifications
{
    NSLog(@"[CurrentViewNotifier] registering for notification");
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(setViewDidAppear:)
                                                 name:@"viewDidAppear"
                                               object:nil];

}

// This should be on the main thread because we want it to block the GUI
- (void)notifyCurrentView
{
    if (currentViewClassName != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:currentViewClassName object:self];
    }
}

- (void)setViewDidAppear:(NSNotification*)notification
{
    currentViewClassName = NSStringFromClass([notification.object class]);
    NSLog(@"[CurrentViewNotifier] %@ class did appear", currentViewClassName);
}

@end
