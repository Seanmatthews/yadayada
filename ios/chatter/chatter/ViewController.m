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
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [[Connection alloc] init];
    ViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:self];
    
    // Connect and register
    ConnectMessage* cm = [[ConnectMessage alloc] init];
    // TODO: get api version programatically
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    
    [self sendMessage:cm];
    
    
    // TEST
    
    RegisterMessage* rm = [[RegisterMessage alloc] init];
    rm.userName = @"sean";
    rm.password = @"sean";
    rm.handle = @"sean";
    
    [MessageUtils serializeMessage:rm];
    
    //TEST
    
    NSLog(@"handle: %@",userHandle);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - incoming and outgoing messages

- (void)sendMessage:(MessageBase*)message
{
    
}

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case RegisterAccept:
            NSLog(@"Register Accept");
            break;
            
        case RegisterReject:
            NSLog(@"Register Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
            break;
            
        case ConnectAccept:
            NSLog(@"Connect Accept");
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
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
            
        case CreateChatroomReject:
            NSLog(@"Create Chatroom Reject");
            break;
    }
}

@end
