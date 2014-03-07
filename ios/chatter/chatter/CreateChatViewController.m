//
//  CreateChatViewController.m
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "CreateChatViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "UIImage+ImageEffects.h"
#import "Messages.h"
#import "ViewController.h"


@interface CreateChatViewController ()

@end

@implementation CreateChatViewController

// Pointer to list in ChatListViewController
@synthesize recentChatroomList;

const double MILES_TO_METERS = 1609.34;


- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        location = [Location sharedInstance];
        ud = [UserDetails sharedInstance];
        chatManager = [ChatroomManagement sharedInstance];
        
        // Get connection object and add this controller's callback
        // method for incoming connections.
        connection = [Connection sharedInstance];
        CreateChatViewController* __weak weakSelf = self;
        [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    _radiusSlider.enabled = YES;
    [_globalChatSelect setSelectedSegmentIndex:1];
}

- (void)viewWillAppear:(BOOL)animated
{
    viewIsVisible = YES;
}

- (void)viewWillDisappear:(BOOL)animated
{
    viewIsVisible = NO;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma  mark - UI Actions

- (IBAction)sliderChanged:(id)sender
{
    UISlider* slider = (UISlider*)sender;
    NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
    [format setNumberStyle:NSNumberFormatterDecimalStyle];
    [format setMaximumFractionDigits:1];
    [format setRoundingIncrement:[NSNumber numberWithFloat:0.1]];
    NSString* str = [format stringFromNumber:[NSNumber numberWithFloat:slider.value]];
    [_chatroomRadiusLabel setText:str];
}

- (IBAction)globalChatSelection:(id)sender
{
    UISegmentedControl* control = (UISegmentedControl*)sender;
    if (control.selectedSegmentIndex == 0) {
        _radiusSlider.enabled = NO;
    }
    else {
        _radiusSlider.enabled = YES;
    }
}


#pragma mark - Keyboard Interaction


- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"segue %@",segue.identifier);
    if ([segue.identifier isEqualToString:@"create2ChatSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatId = chatroomId;
        vc.chatTitle = _chatroomNameTextField.text;
    }
    else if ([segue.identifier isEqualToString:@"unwindToChatList"]) {

    }
}

- (void)joinChatroom:(InviteUserMessage*)ium
{
    [chatManager setGoingToJoin:ium];
    [self performSegueWithIdentifier:@"unwindToChatList" sender:nil];
}


#pragma mark - I/O

- (IBAction)createChatroom:(id)sender
{
    // Check for valid chatroom name
    // Submitting this, in theory, should spawn a reject message
    // from the server, also causing an alert message. But I
    // want to catch errors before they go to the server when possible.
    if ([_chatroomNameTextField.text isEqualToString:@""]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:@"Invalid chatroom name" delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    // Handle check
    if ([ud.handle isEqualToString:@""]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:@"Cannot join a chatroom with no handle" delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    CreateChatroomMessage* msg = [[CreateChatroomMessage alloc] init];
    msg.chatroomName = _chatroomNameTextField.text;
    if (_globalChatSelect.selectedSegmentIndex == 0) {
        msg.radius = 0;
    }
    else {
        msg.radius = [_chatroomRadiusLabel.text floatValue] * MILES_TO_METERS;
    }
    msg.ownerId = ud.userId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    msg.isPrivate = _inviteOnlyControl.selectedSegmentIndex == 0;
    [connection sendMessage:msg];
}

- (void)joinCreatedChatroom
{
    // First, leave current chatroom, if any
    [self leaveCurrentChatroom];
    
    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
    msg.userId = ud.userId;
    msg.chatroomId = chatroomId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}

- (void)leaveCurrentChatroom
{
    if ([chatManager.joinedChatrooms count] > 0) {
        LeaveChatroomMessage* msg = [[LeaveChatroomMessage alloc] init];
        msg.userId = ud.userId;
        msg.chatroomId = [[chatManager.joinedChatrooms allKeys][0] longLongValue];
        [connection sendMessage:msg];
    }
}

- (void)messageCallback:(MessageBase*)message
{
    if (viewIsVisible) {
        UIAlertView *alert;
        switch (message.type) {
            case Chatroom:
                NSLog(@"Chatroom %@",((ChatroomMessage*)message).chatroomName);
                chatroomId = ((ChatroomMessage*)message).chatroomId;
                [recentChatroomList insertObject:message atIndex:0];
                [self joinCreatedChatroom];
                break;
                
            case JoinedChatroom:
                NSLog(@"Joined Chatroom");
                if (((JoinedChatroomMessage*)message).userId ==  ud.userId) {
                    [self performSegueWithIdentifier:@"create2ChatSegue" sender:nil];
                }
                break;
                
            case JoinChatroomReject:
                NSLog(@"Could not join chatroom");
                alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:((JoinChatroomRejectMessage*)message).reason delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                break;
                
            case CreateChatroomReject:
                NSLog(@"Could not create chatroom");
                alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:((CreateChatroomRejectMessage*)message).reason delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                break;
                
            case LeftChatroom:
                NSLog(@"Left Chatroom");
                break;
        }
    }
}

@end
