//
//  MenuViewController.m
//  chatter
//
//  Created by Jim Greco on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuViewController.h"
#import "ViewController.h"

@interface MenuViewController ()

@end

@implementation MenuViewController

@synthesize userHandle;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    ud = [UserDetails sharedInstance];
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"HasFinishedTutorial"]) {
        ud.handle = [[NSUserDefaults standardUserDefaults] stringForKey:@"userHandle"];
        ud.registeredHandle = [[NSUserDefaults standardUserDefaults] boolForKey:@"registeredHandle"];
        NSLog(@"Using handle: %@",ud.handle);
    }
    else {
        // Getting here implies that the user has done the tutorial
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        [[NSUserDefaults standardUserDefaults] setObject:userHandle forKey:@"userHandle"];
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
    
    // Skip this menu view on first load
    //[self performSegueWithIdentifier:@"chatSegue" sender:<#(id)#>]
    
    
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

//- (void) prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
//{
//    NSString * segueName = segue.identifier;
//    if ([segueName isEqualToString: @"menuContainerSegue"]) {
//        UITableViewController* menu = (UITableViewController*)[segue destinationViewController];
//        [menu performSegueWithIdentifier:@"chatSegue" sender:self];
//    }
//}

- (IBAction)unwindToMenu:(UIStoryboardSegue*)unwindSegue
{
    
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

//- (void)registerMessage
//{
//    if (!ud.registeredHandle) {
//        RegisterMessage* rm = [[RegisterMessage alloc] init];
//        rm.handle = @"sean";
//        rm.userName = ud.UUID;
//        rm.password = @"pass";
//        [connection sendMessage:rm];
//        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"registeredhandle"];
//    }
//    else {
//        [self loginMessage];
//    }
//}

- (void)loginMessage
{
    LoginMessage* lm = [[LoginMessage alloc] init];
    lm.userName = ud.UUID;
    lm.password = @"pass";
    [connection sendMessage:lm];
}

- (void)joinGlobalChatroom
{
    JoinChatroomMessage* jcm = [[JoinChatroomMessage alloc] init];
    jcm.userId = ud.userId;
    jcm.chatroomId = ud.chatroomId;
    jcm.latitude = 0;
    jcm.longitude = 0;
    [connection sendMessage:jcm];
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
            [self loginMessage];
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            [self joinGlobalChatroom];
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
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
