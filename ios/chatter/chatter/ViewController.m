//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "Messages.h"



@interface ViewController ()

@end

@implementation ViewController

@synthesize userHandle;
@synthesize userInputTextField;
@synthesize scrollView;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"HasFinishedTutorial"]) {
        ud = [[UserDetails alloc] init];
    }
    else {
        // Getting here implies that the user has done the tutorial
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        ud = [[UserDetails alloc] initWithHandle:userHandle];
    }
    
    userInputTextField.returnKeyType = UIReturnKeySend;
    [self registerForKeyboardNotifications];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [[Connection alloc] init];
    [connection connect];
    ViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];

    // We need this because the run loops of connection don't work until
    // the view is completely loaded.
    [self performSelector:@selector(connectAndRegister) withObject:nil afterDelay:1.0];
    
    NSLog(@"handle: %@",userHandle);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (void)connectAndRegister
{
    NSLog(@"Going to try to connect now");

    ConnectMessage* cm = [[ConnectMessage alloc] init];
    // TODO: get api version programatically
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    [self sendMessage:cm];
    
    RegisterMessage* rm = [[RegisterMessage alloc] init];
    rm.handle = @"sean";
    rm.userName = ud.UUID;
    rm.password = @"pass";
    [self sendMessage:rm];
    
    LoginMessage* lm = [[LoginMessage alloc] init];
    lm.userName = ud.UUID;
    lm.password = @"pass";
    [self sendMessage:lm];
}


#pragma mark - Keyboard Interaction

- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardWillShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillBeHidden:) name:UIKeyboardWillHideNotification object:nil];
}

// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    //UIScrollView* scrollView = (UIScrollView*)[self view];
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    CGRect bkgndRect = userInputTextField.superview.frame;
    // TODO: figure out why this works and the normal way doesn't
    kbSize.height += 100;
    bkgndRect.size.height += kbSize.height;
    [userInputTextField.superview setFrame:bkgndRect];
    [scrollView setContentOffset:CGPointMake(0.0, userInputTextField.frame.origin.y-kbSize.height) animated:YES];
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    CGRect bkgndRect = userInputTextField.superview.frame;
    bkgndRect.size.height -= kbSize.height;
    [userInputTextField.superview setFrame:bkgndRect];
    [scrollView setContentOffset:CGPointMake(0.0, 0.0) animated:YES];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    NSString* text = [textField text];
    if ([text length] > 0) {
        SubmitMessageMessage* sm = [[SubmitMessageMessage alloc] init];
        sm.userId = ud.userId;
        sm.chatroomId = ud.chatroomId;
        sm.message = text;
        [connection sendMessage:sm];
    }
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - incoming and outgoing messages

- (void)sendMessage:(MessageBase*)message
{
    [connection sendMessage:message];
}

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case RegisterAccept:
            NSLog(@"Register Accept");
            ud.userId = ((RegisterAcceptMessage*)message).userId;
            break;
            
        case RegisterReject:
            NSLog(@"Register Reject");
            NSLog(@"%@",((RegisterRejectMessage*)message).reason);
            break;
            
        case ConnectAccept:
            NSLog(@"Connect Accept");
            ud.chatroomId = ((ConnectAcceptMessage*)message).globalChatId;
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
            break;
            
        case Message:
            NSLog(@"Message");
            break;
            
        case Chatroom:
            NSLog(@"Chatroom");
            break;
            
        case JoinChatroomReject:
            NSLog(@"Join Chatroom Reject");
            break;
            
        case JoinedChatroom:
            NSLog(@"Joined Chatroom");
            break;
            
        case LeftChatroom:
            NSLog(@"Left Chatroom");
            break;
    }
}

@end
