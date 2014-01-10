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

@interface ViewController : UIViewController <UITextFieldDelegate, UITableViewDelegate, UITableViewDataSource>
{
    UserDetails* ud;
    Connection* connection;
    NSThread* connectionThread;
    NSString* cellMsgCSS;
    NSString* handleCSS;
    NSString* pageCSS;
    NSString* selfMsgCSS;
    NSString* selfHandleCSS;
    
    
    int swipedCellIndex;
    
    // TODO: this will be an array of arrays of dicts for multiple chat rooms
    NSMutableArray* chatQueue;
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
- (void)registerForKeyboardNotifications;
- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;
- (void)swipeCell:(UITableViewRowAnimation)animation;
- (void)swipedCellLeft:(id)sender;
- (void)swipedCellRight:(id)sender;
- (void)tappedCell:(id)sender;
- (void)receivedMessage:(MessageMessage*) message;
- (UIImage*)blurredSnapshot;
- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId;




@end
