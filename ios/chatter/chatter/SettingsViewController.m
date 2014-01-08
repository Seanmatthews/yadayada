//
//  SettingsViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "SettingsViewController.h"
#import "UIImage+ImageEffects.h"
#import "MenuViewController.h"
#import <QuartzCore/QuartzCore.h>

@interface SettingsViewController ()

@end

@implementation SettingsViewController

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    connection = [Connection sharedInstance];
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
    _handleTextField.text = ud.handle;		
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


#pragma mark - Blurred Snapshot

- (UIImage*)blurredSnapshot
{
    // Create the image context
    UIGraphicsBeginImageContextWithOptions(self.view.bounds.size, YES, self.view.window.screen.scale);
    
    // There he is! The new API method
    [self.view drawViewHierarchyInRect:self.view.frame afterScreenUpdates:NO];
    
    // Get the snapshot
    UIImage *snapshotImage = UIGraphicsGetImageFromCurrentImageContext();
    //[UIImage imageNamed:@"Default@2x.png"];
    
    // Now apply the blur effect using Apple's UIImageEffect category
    UIImage *blurredSnapshotImage = [snapshotImage applyLightEffect];
    
    // Be nice and clean your mess up
    UIGraphicsEndImageContext();
    
    return blurredSnapshotImage;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"unwindSettingsSegue"]) {
        MenuViewController* vc = (MenuViewController*)segue.destinationViewController;
        vc.image =[self blurredSnapshot];
    }
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


#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    ud.handle = [textField text];
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - Message I/O

- (void)messageCallback:(MessageBase*)message
{
    if (viewIsVisible) {
        // TODO
    }
}


@end
