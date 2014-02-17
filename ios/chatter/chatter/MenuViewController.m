//
//  MenuViewController.m
//  chatter
//
//  Created by Sean Matthews on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuViewController.h"
#import "ViewController.h"

@interface MenuViewController ()

@end

@implementation MenuViewController

//@synthesize userHandle;

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    contacts = [Contacts sharedInstance];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    [connection connect];
    MenuViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
    
    //[connection connectToImageServer];
    
    // We need this because the run loops of connection don't work until
    // the view is completely loaded.
    [self performSelector:@selector(connectMessage) withObject:nil afterDelay:1.0];
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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillDisappear:(BOOL)animated
{
    // Since there's no longer a menu, per se, this will only happen once
    [connection removeCallbackBlockFromSender:NSStringFromClass([self class])];
}

- (void)waitForLocation
{
    while (![location didUpdateFirstLocation]) {
        //NSLog(@"waiting");
        usleep(50000);
    }
}


#pragma mark - incoming and outgoing messages

- (void)connectMessage
{
    [self waitForLocation];
    NSLog(@"Going to try to connect now");
    
    ConnectMessage* cm = [[ConnectMessage alloc] init];
    // TODO: get api version programatically
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    [connection sendMessage:cm];
}

- (void)loginMessage
{
    NSLog(@"Logging in with handle: %@",ud.handle);
    QuickLoginMessage* qlm = [[QuickLoginMessage alloc] init];
    qlm.handle = ud.handle;
    qlm.UUID = ud.UUID;
    qlm.phoneNumber = [[contacts getMyPhoneNumber] longLongValue];
    [connection sendMessage:qlm];
}

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case RegisterAccept:
            NSLog(@"Register Accept");
            ud.userId = ((RegisterAcceptMessage*)message).userId;
            [self loginMessage];
            break;
            
        case RegisterReject:
            NSLog(@"Register Reject");
            NSLog(@"%@",((RegisterRejectMessage*)message).reason);
            break;
            
        case ConnectAccept:
            NSLog(@"Connect Accept");
            ud.chatroomId = ((ConnectAcceptMessage*)message).globalChatId;
            ud.iconDownloadURL = ((ConnectAcceptMessage*)message).imageDownloadUrl;
            ud.iconUploadURL = ((ConnectAcceptMessage*)message).imageUploadUrl;
            NSLog(@"global chatroom id: %llx",ud.chatroomId);
            [self loginMessage];
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            ud.userId = ((RegisterAcceptMessage*)message).userId;
            [self performSegueWithIdentifier:@"chatListSegue" sender:nil];
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
            NSLog(@"%@",((LoginRejectMessage*)message).reason);
            break;
    }
}


@end
