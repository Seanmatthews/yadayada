//
//  CreateChatViewController.m
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "CreateChatViewController.h"
#import "Messages.h"
#import "ViewController.h"


@interface CreateChatViewController ()

@end

@implementation CreateChatViewController


- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        location = [Location sharedInstance];
        ud = [UserDetails sharedInstance];
        
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
    [format setMaximumFractionDigits:2];
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
    if ([segue.identifier isEqualToString:@"create2ChatSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatId = chatroomId;
        vc.chatTitle = _chatroomNameTextField.text;
    }
}

- (IBAction)unwindToPreviousView:(id)sender
{
    NSLog(@"%@",_unwindSegueName);
    [self performSegueWithIdentifier:_unwindSegueName sender:self];
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
    
    CreateChatroomMessage* msg = [[CreateChatroomMessage alloc] init];
    msg.chatroomName = _chatroomNameTextField.text;
    if (_globalChatSelect.selectedSegmentIndex == 0) {
        msg.radius = 0;
    }
    else {
        msg.radius = [_chatroomRadiusLabel.text floatValue];
    }
    msg.ownerId = ud.userId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}

- (void)joinCreatedChatroom
{
    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
    msg.userId = ud.userId;
    msg.chatroomId = chatroomId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
    
}

- (void)messageCallback:(MessageBase*)message
{
    UIAlertView *alert;
    switch (message.type) {
        case Chatroom:
            NSLog(@"Chatroom %@",((ChatroomMessage*)message).chatroomName);
            chatroomId = ((ChatroomMessage*)message).chatroomId;
            [self joinCreatedChatroom];
            break;
            
        case JoinedChatroom:
            NSLog(@"Joined Chatroom");
            [self performSegueWithIdentifier:@"create2ChatSegue" sender:nil];
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

@end
