//
//  SearchUsersViewController.h
//  chatter
//
//  Created by sean matthews on 6/8/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Chatroom.h"

@interface SearchUsersViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>

@property (strong, nonatomic) IBOutlet UISearchDisplayController* searchBarController;
@property (strong, nonatomic) IBOutlet UISearchBar *searchBar;
@property (strong, nonatomic) IBOutlet UITableView *tblContentList;
@property (strong, nonatomic) Chatroom* inviteChatroom;

@end
