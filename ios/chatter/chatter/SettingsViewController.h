//
//  SettingsViewController.h
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Connection.h"

@interface SettingsViewController : UIViewController
{
    Connection* connection;
}

@property (nonatomic,retain) IBOutlet UITableView* tableView;

- (void)initCode;

@end
