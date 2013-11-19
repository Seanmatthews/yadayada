//
//  MenuViewController.m
//  chatter
//
//  Created by Jim Greco on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuViewController.h"
#import "ViewController.h"
#import "MenuTableViewController.h"

@interface MenuViewController ()

@end

@implementation MenuViewController

@synthesize userHandle;

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"HasFinishedTutorial"]) {
        ud.handle = [[NSUserDefaults standardUserDefaults] stringForKey:@"userHandle"];
        ud.registeredHandle = [[NSUserDefaults standardUserDefaults] boolForKey:@"registeredHandle"];
        NSLog(@"Using handle: %@",ud.handle);
    }
    else {
        // Getting here implies that the user has done the tutorial
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
        [[NSUserDefaults standardUserDefaults] setObject:userHandle forKey:@"userHandle"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        ud.handle = userHandle;
    }
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    [connection connect];
    MenuViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
    
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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated
{
    _bgImageView.image = _image;
}


#pragma mark - Segues

- (IBAction)unwindToMenu:(UIStoryboardSegue*)unwindSegue
{
    
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    containerView = (MenuTableViewController*)segue.destinationViewController;
}


#pragma mark - incoming and outgoing messages

- (void)connectMessage
{
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
    [connection sendMessage:qlm];
}

//- (void)joinGlobalChatroom
//{
//    JoinChatroomMessage* jcm = [[JoinChatroomMessage alloc] init];
//    jcm.userId = ud.userId;
//    jcm.chatroomId = ud.chatroomId;
//    jcm.latitude = 0;
//    jcm.longitude = 0;
//    [connection sendMessage:jcm];
//}

static BOOL onceToChatListView = YES;
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
            NSLog(@"global chatroom id: %llx",ud.chatroomId);
            [self loginMessage];
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            ud.userId = ((RegisterAcceptMessage*)message).userId;
            if (onceToChatListView) {
                onceToChatListView = NO;
                [containerView performSegueWithIdentifier:@"chatListSegue" sender:containerView];
            }
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
            NSLog(@"%@",((LoginRejectMessage*)message).reason);
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
    }
}





@end
