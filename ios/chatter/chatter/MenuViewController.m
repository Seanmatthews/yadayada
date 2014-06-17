//
//  MenuViewController.m
//  chatter
//
//  Created by Sean Matthews on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuViewController.h"
#import "ViewController.h"
#import "ChatroomManagement.h"

@interface MenuViewController ()
{
    
}

- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)receivedFirstLocation;
- (void)receivedConnectAccept:(NSNotification*)notification;
- (void)receivedConnectReject:(NSNotification*)notification;
- (void)receivedLoginAccept:(NSNotification*)notification;
- (void)receivedLoginReject:(NSNotification*)notification;
- (void)receivedJoinedChatroom:(NSNotification*)notification;
- (void)cannotConnect;
- (void)errorUI:(BOOL)hidden;
- (void)fillErrorMessageArray;

@end


@implementation MenuViewController
{
    ChatroomManagement* chatManager;
    NSTimeInterval heartbeatInterval;
    NSArray* badConnectionMessages;
    BOOL connected;
}


- (void)fillErrorMessageArray
{
    badConnectionMessages = @[@"I feel like we're just not communicating.",
                              @"Can you speak up?",
                              @"It's not me, it's you.",
                              @"I think you know what the problem is just as well as I do.",
                              @"Daisy, Daisy, give me your answer do."];
}

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    contacts = [Contacts sharedInstance];
    connection = [Connection sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    [self registerForNotifications];
}

- (void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(receivedFirstLocation)
                                                 name:@"LocationUpdateNotification"
                                               object:nil];
    
    for (NSString* notificationName in @[@"ConnectAccept", @"LoginReject", //@"JoinedChatroom",
                                         @"ConnectReject", @"LoginAccept"]) {
        
        NSString* selectorName = [NSString stringWithFormat:@"received%@:",notificationName];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:NSSelectorFromString(selectorName)
                                                     name:[NSString stringWithFormat:@"%@Message",notificationName]
                                                   object:nil];
    }
}

- (void)unregisterForNotifications
{
    // We will always remove ourselves from seeing all notifications in this view.
    [[NSNotificationCenter defaultCenter] removeObserver:self];
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
    
    // This does not work when placed in initCode()
    if (!ud.finishedTutorial) {
        // Getting here implies that the user has done the tutorial for the first time
        ud.finishedTutorial = YES;
        //ud.handle = userHandle;

        // Maybe not necessary, but just in case
        [UserDetails save];
    }
    
    _errorLabel.numberOfLines = 0;
    _errorLabel.lineBreakMode = NSLineBreakByWordWrapping;
    
    connected = NO;
    [self fillErrorMessageArray];
    [self errorUI:YES];
    [self performSelector:@selector(cannotConnect) withObject:nil afterDelay:10.];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self unregisterForNotifications];
}

- (void)cannotConnect
{
    if (!connected) {
        _errorLabel.text = [NSString stringWithFormat:@"%@\nBad connection",
                            badConnectionMessages[arc4random() % badConnectionMessages.count]];
        _errorLabel.textAlignment = NSTextAlignmentCenter;
        [self errorUI:NO];
    }
}

- (void)errorUI:(BOOL)hidden
{
    _infoLabel.hidden = !hidden;
    _errorLabel.hidden = hidden;
    _retryButton.hidden = hidden;
    _spinner.hidden = !hidden;
}

- (IBAction)retryConnection:(id)sender
{
    [self performSelector:@selector(cannotConnect) withObject:nil afterDelay:10.];
    [self errorUI:YES];
    NSLog(@"Going to try to connect again");
    [connection connectWithNoReconnect];
    ConnectMessage* cm = [[ConnectMessage alloc] init];
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    [connection sendMessage:cm];
}


#pragma mark - incoming and outgoing messages

- (void)receivedFirstLocation
{
    NSLog(@"lat: %f, long: %f",[location currentLocation].latitude, [location currentLocation].longitude);
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"LocationUpdateNotification" object:nil];
    
    NSLog(@"Going to try to connect now");
    ConnectMessage* cm = [[ConnectMessage alloc] init];
    // TODO: get api version programatically
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    [connection sendMessage:cm];
}

- (void)receivedConnectAccept:(NSNotification*)notification
{
    NSLog(@"Connect Accept");
    connected = YES;
    ConnectAcceptMessage* cam = notification.object;
    [chatManager setGlobalChatroomId:[NSNumber numberWithLongLong:cam.globalChatId]];
    ud.iconDownloadURL = cam.imageDownloadUrl;
    ud.iconUploadURL = cam.imageUploadUrl;
    heartbeatInterval = cam.heartbeatInterval;
    
    QuickLoginMessage* qlm = [[QuickLoginMessage alloc] init];
    qlm.handle = ud.handle;
    qlm.UUID = ud.UUID;
    qlm.phoneNumber = [[contacts getMyPhoneNumber] longLongValue];
    //#warning Assumes data is already null terminated
    qlm.deviceToken = [NSString stringWithFormat:@"%@",ud.deviceToken]; //[NSString stringWithUTF8String:[ud.deviceToken bytes]];
    [connection sendMessage:qlm];
    NSLog(@"[MenuViewController] %@ %@", qlm.handle, qlm.UUID);
}

- (void)receivedConnectReject:(NSNotification*)notification
{
    NSLog(@"Connect Reject");
    NSLog(@"%@",[notification.object reason]);
    
    // TODO: uialertview with try again button
}

- (void)receivedLoginAccept:(NSNotification*)notification
{
    NSLog(@"Login Accept");
    ud.userId = [notification.object userId];
    [chatManager searchChatrooms];
    [connection setHeartbeatInterval:heartbeatInterval];
    
//    // Join global chatroom automatically on startup
//    [chatManager leaveChatroomWithId:chatManager.globalChatroomId withCompletion:nil];
//    [chatManager joinChatroomWithId:chatManager.globalChatroomId withCompletion:nil];
    [self performSegueWithIdentifier:@"chatListSegue" sender:nil];
}

- (void)receivedLoginReject:(NSNotification*)notification
{
    NSLog(@"Login Reject");
    NSLog(@"%@",[notification.object reason]);
    
    // TODO: uialrtview-- need to give user a chance to change their handle..
    // that's the only reason they would be rejected from logging in.
}

- (void)receivedJoinedChatroom:(NSNotification*)notification
{
//    [self performSegueWithIdentifier:@"chatListSegue" sender:nil];
}

@end
