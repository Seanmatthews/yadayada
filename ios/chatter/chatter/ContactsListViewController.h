//
//  ContactsListViewController.h
//  chatter
//
//  Created by sean matthews on 2/22/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Contacts.h"

@interface ContactsListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, UIBarPositioningDelegate>
{
    Contacts* contacts;
}

@property (nonatomic,retain) IBOutlet UITableView* tableView;


- (void)initCode;

@end
