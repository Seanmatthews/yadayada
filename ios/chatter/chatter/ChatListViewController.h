//
//  ChatListViewController.h
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Location.h"
#import "Messages.h"
#import "Connection.h"
#import "UserDetails.h"


@interface ChatListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource>
{
    Location* location;
    Connection* connection;
    UserDetails* ud;
    NSMutableArray* localChatroomList;
    NSMutableArray* globalChatroomList;
    ChatroomMessage *tappedCellInfo;
}

@property (nonatomic,retain) IBOutlet UITableView* tableView;

// Navigation property
@property (nonatomic) BOOL jumpToGlobalChat;

- (void)initCode;
- (void)messageCallback:(MessageBase*)message;
- (void)refreshTable:(UIRefreshControl*)refreshControl;
- (void)searchChatrooms;


@end
