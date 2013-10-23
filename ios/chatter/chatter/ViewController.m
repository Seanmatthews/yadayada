//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"

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
    
    NSLog(@"handle: %@",userHandle);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
