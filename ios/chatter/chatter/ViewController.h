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
    
    // TODO: this will be an array of arrays of dicts for multiple chat rooms
    NSMutableArray* chatQueue;
}

//@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic, retain) IBOutlet UITextField* userInputTextField;
@property (nonatomic, retain) IBOutlet UIScrollView* scrollView;
@property (nonatomic, retain) IBOutlet UITableView* mTableView;


- (void)messageCallback:(MessageBase*)message;
- (void)registerForKeyboardNotifications;
- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;
- (void)tappedCell:(id)sender;
- (void)swipedCell:(id)sender;
- (void)receivedMessage:(MessageMessage*) message;
- (UIImage*)blurredSnapshot;



@end
