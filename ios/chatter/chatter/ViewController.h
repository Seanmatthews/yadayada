//
//  ViewController.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"
#import "MessageUtils.h"
#import "Connection.h"
#import "UITableViewCell+UITableViewCellCategory.h"
#import "UITableView+UITableViewCategory.h"
#import "Messages.h"
#import "MenuViewController.h"
#import "ChatroomManagement.h"
#import "Contacts.h"
#import <AddressBookUI/AddressBookUI.h>

@interface ViewController : UIViewController <UITextFieldDelegate, UITableViewDelegate, UITableViewDataSource, ABPeoplePickerNavigationControllerDelegate>
{
    UserDetails* ud;
    Connection* connection;
    Contacts* contacts;
    NSThread* connectionThread;
    NSString* cellMsgCSS;
    NSString* handleCSS;
    NSString* pageCSS;
    NSString* selfMsgCSS;
    NSString* selfHandleCSS;
    
    int swipedCellIndex;
    
//    NSMutableArray* chatQueue;
    ChatroomManagement* chatManager;
}

//@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic, retain) IBOutlet UITextField* userInputTextField;
@property (nonatomic, retain) IBOutlet UITableView* mTableView;
@property (nonatomic, retain) IBOutlet UINavigationBar* navBar;

// Passed from chat list view
@property (nonatomic, strong) NSString* chatTitle;
@property (nonatomic) long long chatId;


- (void)initCode;
- (void)messageCallback:(MessageBase*)message;
- (void)refreshMessages;
- (void)registerForKeyboardNotifications;
- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;
- (void)swipeCell:(UITableViewRowAnimation)animation;
- (void)swipedCellLeft:(id)sender;
- (void)swipedCellRight:(id)sender;
- (void)tappedCell:(id)sender;
- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId;
- (IBAction)unwindToChatroom:(UIStoryboardSegue*)unwindSegue;
- (IBAction)inviteUser:(id)sender;




@end
