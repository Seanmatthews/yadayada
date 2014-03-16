//
//  ViewController.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import UIKit;
@import AddressBookUI;
#import "UserDetails.h"
#import "MessageUtils.h"
#import "Connection.h"
#import "UITableViewCell+UITableViewCellCategory.h"
#import "UITableView+UITableViewCategory.h"
#import "Messages.h"
#import "Chatroom.h"
#import "Contacts.h"

@interface ViewController : UIViewController <UITextFieldDelegate, UITableViewDelegate, UITableViewDataSource, ABPeoplePickerNavigationControllerDelegate, UIBarPositioningDelegate>


//@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic, retain) IBOutlet UITextField* userInputTextField;
@property (nonatomic, retain) IBOutlet UITableView* mTableView;
@property (nonatomic, retain) IBOutlet UINavigationBar* navBar;

// Passed from chat list view
@property (nonatomic, strong) Chatroom* chatroom;


- (void)initCode;
- (void)swipeCell:(UITableViewRowAnimation)animation;
- (void)swipedCellLeft:(id)sender;
- (void)swipedCellRight:(id)sender;
- (void)tappedCell:(id)sender;
- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId;
- (IBAction)unwindToChatroom:(UIStoryboardSegue*)unwindSegue;

@end
