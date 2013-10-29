//
//  ViewController.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"
#import "MessageUtils.h"
#import "Connection.h"

@interface ViewController : UIViewController
{
    UserDetails* ud;
    Connection* connection;
    NSThread* connectionThread;
}

@property (nonatomic, strong) NSString* userHandle;

- (void)messageCallback:(MessageBase*)message;
- (void)connectionThreadMethod:(Connection*)connection;

@end
