//
//  ViewController.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"

@interface ViewController : UIViewController
{
    UserDetails* ud;
}

@property (nonatomic, strong) NSString* userHandle;

@end
