//
//  AppDelegate.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "AppDelegate.h"

@implementation AppDelegate

const NSTimeInterval LOCATE_INTERVAL = 30.;
const NSTimeInterval LOCATE_DURATION = 3.;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
#if DEBUG
    NSLog(@"Debug Mode");
#else
    NSLog(@"Release Mode");
#endif
    
    // start location service
    location = [Location sharedInstance];
    [location startServiceWithInterval:LOCATE_INTERVAL andDuration:LOCATE_DURATION];
    
    // Load global singletons
    ud = [UserDetails sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    contacts = [Contacts sharedInstance];
    
    connection = [Connection sharedInstance];
    [connection connect];
    
    // Do this here?
//    [contacts getAddressBookPermissions];
    
    UIStoryboard *uis = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    self.window = [[UIWindow alloc]
                   initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.backgroundColor = [UIColor whiteColor];
    UIViewController* uvc;
    
    if (ud.finishedTutorial) {
        uvc = [uis instantiateViewControllerWithIdentifier:@"mainPage"];
    }
    else {
        UIPageControl *pageControl = [UIPageControl appearance];
        pageControl.pageIndicatorTintColor = [UIColor lightGrayColor];
        pageControl.currentPageIndicatorTintColor = [UIColor blackColor];
        pageControl.backgroundColor = [UIColor whiteColor];
        uvc = [uis instantiateViewControllerWithIdentifier:@"TutorialStart"];
    }
    self.window.rootViewController = uvc;
    [self.window makeKeyAndVisible];
    
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    [UserDetails save];
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    [UserDetails save];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    [UserDetails save];
}

@end
