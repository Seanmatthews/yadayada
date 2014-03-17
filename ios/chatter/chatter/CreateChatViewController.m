//
//  CreateChatViewController.m
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "CreateChatViewController.h"
#import "Connection.h"
#import "Location.h"
#import "UserDetails.h"
#import "ChatroomManagement.h"
#import "ViewController.h"


@interface CreateChatViewController ()

@end

@implementation CreateChatViewController
{
    Connection* connection;
    Location* location;
    UserDetails* ud;
    ChatroomManagement* chatManager;
}

const double MILES_TO_METERS = 1609.34;


- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        location = [Location sharedInstance];
        ud = [UserDetails sharedInstance];
        chatManager = [ChatroomManagement sharedInstance];
        connection = [Connection sharedInstance];
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
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar {
    return UIBarPositionTopAttached;
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

- (IBAction)createChatroom:(id)sender
{
    // Check for valid chatroom name
    // Submitting this, in theory, should spawn a reject message
    // from the server, also causing an alert message. But I
    // want to catch errors before they go to the server when possible.
    if ([_chatroomNameTextField.text isEqualToString:@""]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Woops!"
                                                        message:@"Invalid chatroom name"
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    // Handle check
    if ([ud.handle isEqualToString:@""]) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Woops!"
                                                        message:@"Cannot join a chatroom with no handle"
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    __block Chatroom* chatroom = [[Chatroom alloc] init];
    chatroom.chatroomName = _chatroomNameTextField.text;
    chatroom.origin = [location currentLocation];
    chatroom.private = _inviteOnlyControl.selectedSegmentIndex == 0;
    chatroom.global = _globalChatSelect.selectedSegmentIndex == 0;
    if (chatroom.global) {
        chatroom.radius = 0;
    }
    else {
        chatroom.radius = [NSNumber numberWithLongLong:[_chatroomRadiusLabel.text floatValue] * MILES_TO_METERS];
    }
    [chatManager createChatroom:chatroom withCompletion:^(long long cid){
        chatroom.cid = [NSNumber numberWithLongLong:cid];
        [chatManager joinChatroom:chatroom withCompletion:^{
            Chatroom* created = [[[ChatroomManagement sharedInstance] chatrooms] objectForKey:chatroom.cid];
            [self performSegueWithIdentifier:@"createChatroomSegue" sender:created];
        }];
    }];
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
    if ([segue.identifier isEqualToString:@"createChatroomSegue"]) {
//        [segue.destinationViewController setChatroom:sender];
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatroom = sender;
    }
}


@end
