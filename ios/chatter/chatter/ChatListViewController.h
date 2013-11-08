//
//  ChatListViewController.h
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Location.h"

@interface ChatListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>
{
    Location* location;
}

@property (nonatomic,retain) IBOutlet UITableView* tableView;

@end
