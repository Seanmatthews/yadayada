//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "Messages.h"
#import "MessageUtils.h"


@interface ViewController ()

@end

@implementation ViewController

@synthesize userHandle;

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Getting here implies that the user has done the tutorial
	[[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
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

@end
