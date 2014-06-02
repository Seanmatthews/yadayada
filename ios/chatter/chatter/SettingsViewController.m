//
//  SettingsViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "SettingsViewController.h"
#import "Messages.h"
#import "ViewController.h"


@interface SettingsViewController ()

//- (void)reregisterHandle;
- (void)changeHandle:(NSString*)handle;
- (void)registerForKeyboardNotifications;
- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)receivedChangeHandleAccept:(NSNotification*)notification;
- (void)receivedChangeHandleReject:(NSNotification*)notification;
- (void)segueToChatroom:(NSNotification*)notification;
//- (void)leaveAllChatrooms;

@end

@implementation SettingsViewController
{
    Connection* connection;
    UserDetails* ud;
    UIImageView* iconView;
    ChatroomManagement* chatManager;
}

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    connection = [Connection sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    [self registerForKeyboardNotifications];
    
}

- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
}

- (void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(segueToChatroom:)
                                                 name:@"segueToChatroomNotification"
                                               object:nil];
    
    for (NSString* notificationName in @[@"ChangeHandleAccept", @"ChangeHandleReject", @"InviteUser"]) {
        NSString* selectorName = [NSString stringWithFormat:@"received%@:",notificationName];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:NSSelectorFromString(selectorName)
                                                     name:[NSString stringWithFormat:@"%@Message",notificationName]
                                                   object:nil];
    }
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.view setAutoresizingMask:UIViewAutoresizingFlexibleHeight];
    [_scrollView setAutoresizingMask:UIViewAutoresizingFlexibleHeight];
    _handleTextField.text = ud.handle;
	_handleTextField.returnKeyType = UIReturnKeyDone;
    [_chatroomNotificationControl setSelectedSegmentIndex:(ud.receiveChatroomNotifications ? 0 : 1)];
    [_messageNotificationControl setSelectedSegmentIndex:(ud.receiveMessageNotifications ? 0 : 1)];
    [_inviteNotificationControl setSelectedSegmentIndex:(ud.receiveInviteNotifications ? 0 : 1)];
    
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self registerForNotifications];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self unregisterForNotifications];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar {
    return UIBarPositionTopAttached;
}


#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"unwindToChatList"]) {

    }
    else if ([segue.identifier isEqualToString:@"settings2ChatroomSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatroom = sender;
    }
//    if ([segue.identifier isEqualToString:@"settingsEmbedSegue"]) {
//        DragImageController* dic = (DragImageController*)segue.destinationViewController;
//        if (ud.userIcon) {
//            dic.imageView.image = ud.userIcon;
//        }
//        
//        // ARC is enabled, but is this being retained?
//        iconView = dic.imageView;
//    }
}

- (void)segueToChatroom:(NSNotification*)notification
{
    [self performSegueWithIdentifier:@"settings2ChatroomSegue" sender:notification.object];
}


#pragma mark - UI elements

- (IBAction)chatroomNotificationValueChanged:(id)sender
{
    UISegmentedControl* sc = (UISegmentedControl*)sender;
    ud.receiveChatroomNotifications = sc.selectedSegmentIndex == 0;
}

- (IBAction)messageNotificationValueChanged:(id)sender
{
    UISegmentedControl* sc = (UISegmentedControl*)sender;
    ud.receiveMessageNotifications = sc.selectedSegmentIndex == 0;
}

- (IBAction)inviteNotificationValueChanged:(id)sender
{
    UISegmentedControl* sc = (UISegmentedControl*)sender;
    ud.receiveInviteNotifications = sc.selectedSegmentIndex == 0;
}

- (IBAction)applySettingsButtonPressed:(id)sender
{
    if (![_handleTextField.text isEqualToString:ud.handle]) {
        [self changeHandle:_handleTextField.text];
    }
//    else {
//        [self performSegueWithIdentifier:@"unwindToChatList" sender:nil];
//    }
    
    //UIImage* img = iconView.image;
    //[connection uploadImage:[UIImage imageNamed:@"lena.jpg"] forUserId:ud.userId toURL:ud.iconUploadURL];
}


#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWasShown:)
                                                 name:UIKeyboardWillShowNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
}

// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification
{
//    NSDictionary* info = [aNotification userInfo];
//    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
//    
//    // Animate the current view out of the way
//    [UIView beginAnimations:nil context:NULL];
//    [UIView setAnimationDuration:0.3]; // if you want to slide up the view
//    CGRect rect = self.view.frame;
//    rect.origin.y -= kbSize.height;
//    self.view.frame = rect;
//    [UIView commitAnimations];
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
//    NSDictionary* info = [aNotification userInfo];
//    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
//    
//    // Animate the current view back to where it was
//    [UIView beginAnimations:nil context:NULL];
//    [UIView setAnimationDuration:0.3]; // if you want to slide up the view
//    CGRect rect = self.view.frame;
//    rect.origin.y += kbSize.height;
//    self.view.frame = rect;
//    [UIView commitAnimations];
}


#pragma mark - Message I/O

// Changed handle and settings successfully. Revert to chat list view.
- (void)receivedChangeHandleAccept:(NSNotification*)notification
{
    NSLog(@"[SettingsViewController] handle successfully changed");
    ud.handle = [notification.object handle];
//    [self performSegueWithIdentifier:@"unwindToChatList" sender:nil];
}

// Handle change was unsuccessful. Stay on the view.
- (void)receivedChangeHandleReject:(NSNotification*)notification
{
    [_handleTextField setText:ud.handle];
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Woops!"
                                                    message:[notification.object reason]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}

//- (void)leaveAllChatrooms
//{
//    for (Chatroom* c in [chatManager joinedChatrooms]) {
//        LeaveChatroomMessage* msg = [[LeaveChatroomMessage alloc] init];
//        msg.userId = ud.userId;
//        msg.chatroomId = [c.cid longLongValue];
//        [connection sendMessage:msg];
//    }
//}

- (void)changeHandle:(NSString*)handle
{
    ChangeHandleMessage* chm = [[ChangeHandleMessage alloc] init];
    chm.oldHandle = ud.handle;
    chm.handle = handle;
    chm.userId = ud.userId;
    [connection sendMessage:chm];
}

//- (void)reregisterHandle
//{
//    // Leave current chatrooms
//    [self leaveAllChatrooms];
//    
//    NSLog(@"Logging in with handle: %@",_handleTextField.text);
//    QuickLoginMessage* qlm = [[QuickLoginMessage alloc] init];
//    qlm.handle = _handleTextField.text;
//    qlm.UUID = ud.UUID;
//    qlm.phoneNumber = [[[Contacts sharedInstance] getMyPhoneNumber] longLongValue];
//    //#warning Assumes data is already null terminated
//    qlm.deviceToken = [NSString stringWithFormat:@"%@",ud.deviceToken]; //[NSString stringWithUTF8String:[ud.deviceToken bytes]];
//    [connection sendMessage:qlm];
//}


@end
