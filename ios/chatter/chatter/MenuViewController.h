//
//  MenuViewController.h
//  chatter
//
//  Created by Jim Greco on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"
#import "Messages.h"
#import "Connection.h"
#import "Location.h"

@interface MenuViewController : UIViewController 
{
    UserDetails* ud;
    Connection* connection;
    BOOL alreadyRegistered;
    UITableViewController* containerView;
    Location* location;
}

@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic, strong) UIImage* image;
@property (nonatomic, retain) IBOutlet UIImageView* bgImageView;
@property (nonatomic, retain) IBOutlet UITableView* menuTableView;
@property (nonatomic, retain) IBOutlet UIView* waitingView;
@property (nonatomic, retain) IBOutlet UIActivityIndicatorView* indicator;

- (void)initCode;
- (void)connectMessage;
- (void)loginMessage;
- (void)messageCallback:(MessageBase*)message;
- (void)waitForLocation;
- (IBAction)unwindToMenu:(UIStoryboardSegue*)unwindSegue;


@end
