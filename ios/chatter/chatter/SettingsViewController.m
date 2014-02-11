//
//  SettingsViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "SettingsViewController.h"
#import "UIImage+ImageEffects.h"
#import "Messages.h"


@interface SettingsViewController ()

@end

@implementation SettingsViewController

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    connection = [Connection sharedInstance];
    [self registerForKeyboardNotifications];
    SettingsViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
}

- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
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
}

- (void)viewWillAppear:(BOOL)animated
{
    viewIsVisible = YES;
}

- (void)viewWillDisappear:(BOOL)animated
{
    if ([self isBeingDismissed]) {
        [connection removeCallbackBlockFromSender:NSStringFromClass([self class])];
    }
    viewIsVisible = NO;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"settingsEmbedSegue"]) {
        DragImageController* dic = (DragImageController*)segue.destinationViewController;
        if (ud.userIcon) {
            dic.imageView.image = ud.userIcon;
        }
        
        // ARC is enabled, but is this being retained?
        iconView = dic.imageView;
    }
}

- (IBAction)unwindToPreviousView:(id)sender
{
    NSLog(@"%@",_unwindSegueName);
    [self performSegueWithIdentifier:_unwindSegueName sender:self];
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

- (IBAction)applySettingsButtonPressed:(id)sender
{
    [self reregisterHandle];
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
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillBeHidden:) name:UIKeyboardWillHideNotification object:nil];
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

- (void)messageCallback:(MessageBase*)message
{
    if (viewIsVisible) {
        UIAlertView* alert;
        switch (message.type) {
                
            case LoginAccept:
                ud.handle = [_handleTextField text];
                break;
                
            case LoginReject:
                [_handleTextField setText:ud.handle];
                alert = [[UIAlertView alloc] initWithTitle:@"Woops!" message:((LoginRejectMessage*)message).reason delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                break;
        }
    }
}

- (void)reregisterHandle
{
    NSLog(@"Logging in with handle: %@",ud.handle);
    QuickLoginMessage* qlm = [[QuickLoginMessage alloc] init];
    qlm.handle = ud.handle;
    qlm.UUID = ud.UUID;
    [connection sendMessage:qlm];
}


@end
