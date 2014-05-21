//
//  CurrentViewNotifier.h
//  chatter
//
//  Created by sean matthews on 5/20/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CurrentViewNotifier : NSObject

+ (id)sharedNotifier;
- (void)registerForNotifications;
- (void)notifyCurrentView;

@end
