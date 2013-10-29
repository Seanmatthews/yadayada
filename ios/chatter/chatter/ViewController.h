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

@interface ViewController : UIViewController <UITextFieldDelegate>
{
    UserDetails* ud;
    Connection* connection;
    NSThread* connectionThread;
}

@property (nonatomic, strong) NSString* userHandle;
@property (nonatomic, retain) IBOutlet UITextField* userInputTextField;
@property (nonatomic, retain) IBOutlet UIScrollView* scrollView;

- (void)messageCallback:(MessageBase*)message;
- (void)connectAndRegister;
- (void)registerForKeyboardNotifications;
- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;


@end
