//
//  CreateChatViewController.m
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "CreateChatViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "Messages.h"
#import "ViewController.h"


@interface CreateChatViewController ()

- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)receivedChatroom:(NSNotification*)notification;
- (void)receivedCreateChatroomReject:(NSNotification*)notification;
- (void)receivedJoinedChatroom:(NSNotification*)notification;
- (void)segueToChatroom:(NSNotification*)notification;

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

- (void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(segueToChatroom:)
                                                 name:@"segueToChatroomNotification"
                                               object:nil];
    
    for (NSString* notificationName in @[@"Chatroom", @"CreateChatroomReject", @"JoinedChatroom"]) {
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
    _radiusSlider.enabled = YES;
    [_globalChatSelect setSelectedSegmentIndex:1];
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

- (void)segueToChatroom:(NSNotification*)notification
{
    // Not really creating a chatroom... consider renaming the segue
    [self performSegueWithIdentifier:@"createChatroomSegue" sender:notification.object];
}


#pragma mark - I/O

- (void)receivedChatroom:(NSNotification*)notification
{
    JoinChatroomMessage* jcm = [[JoinChatroomMessage alloc] init];
    jcm.userId = ud.userId;
    jcm.chatroomId = [notification.object chatroomId];
    jcm.latitude = [location currentLat];
    jcm.longitude = [location currentLong];
    [connection sendMessage:jcm];
}

- (void)receivedCreateChatroomReject:(NSNotification*)notification
{
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle: @"Woops!"
                                                    message:[notification.object reason]
                                                   delegate:nil
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}

- (void)receivedJoinedChatroom:(NSNotification*)notification
{
    NSLog(@"joined chatroom");
    Chatroom* c = [chatManager.chatrooms objectForKey:[NSNumber numberWithLongLong:[notification.object chatroomId]]];
    [self performSegueWithIdentifier:@"createChatroomSegue" sender:c];
}


@end
