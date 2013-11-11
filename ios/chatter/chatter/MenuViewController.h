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

@interface MenuViewController : UIViewController 
{
    UserDetails* ud;
    Connection* connection;
    BOOL alreadyRegistered;
}

@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic,strong) UIImage* image;
@property (nonatomic,retain) IBOutlet UIImageView* bgImageView;
@property (nonatomic, retain) IBOutlet UITableView* menuTableView;

- (void)connectMessage;
- (void)registerMessage;
- (void)loginMessage;
- (void)joinGlobalChatroom;
- (void)messageCallback:(MessageBase*)message;
- (IBAction)unwindToMenu:(UIStoryboardSegue*)unwindSegue;


@end
