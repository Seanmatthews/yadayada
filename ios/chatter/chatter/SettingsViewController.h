//
//  SettingsViewController.h
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Connection.h"
#import "UserDetails.h"
#import "DragImageController.h"
#import "ChatroomManagement.h"

@interface SettingsViewController : UIViewController <UITextFieldDelegate, UIBarPositioningDelegate>
{
    Connection* connection;
    UserDetails* ud;
    BOOL viewIsVisible;
    UIImageView* iconView;
    ChatroomManagement* chatManager;
}

@property (nonatomic,retain) IBOutlet UITextField* handleTextField;
@property (nonatomic,retain) IBOutlet UISegmentedControl* chatroomNotificationControl;
@property (nonatomic,retain) IBOutlet UISegmentedControl* messageNotificationControl;
@property (nonatomic,retain) IBOutlet UIScrollView* scrollView;
@property (nonatomic,retain) IBOutlet UISegmentedControl* inviteNotificationControl;
@property (nonatomic,strong) NSString* unwindSegueName;


- (void)initCode;
- (void)messageCallback:(MessageBase*)message;
- (void)reregisterHandle;
- (void)registerForKeyboardNotifications;
- (IBAction)chatroomNotificationValueChanged:(id)sender;
- (IBAction)messageNotificationValueChanged:(id)sender;
- (IBAction)applySettingsButtonPressed:(id)sender;
- (void)leaveCurrentChatroom;
- (void)joinChatroom:(InviteUserMessage*)ium;

@end
