//
//  AppDelegate.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Location.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>
{
    Location* location;
}

@property (strong, nonatomic) UIWindow *window;

@end
