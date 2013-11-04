//
//  MenuViewController.h
//  chatter
//
//  Created by Jim Greco on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"

@interface MenuViewController : UIViewController
{
    UserDetails* ud;
}

@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic,strong) UIImage* image;
@property (nonatomic,retain) IBOutlet UIImageView* bgImageView;

@end
