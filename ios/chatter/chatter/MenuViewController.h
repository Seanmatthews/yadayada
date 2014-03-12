//
//  MenuViewController.h
//  chatter
//
//  Created by Sean Matthews on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"
#import "Messages.h"
#import "Connection.h"
#import "Location.h"
#import "Contacts.h"

@interface MenuViewController : UIViewController 
{
    UserDetails* ud;
    Connection* connection;
    Location* location;
    Contacts* contacts;
}

- (void)initCode;

@end
