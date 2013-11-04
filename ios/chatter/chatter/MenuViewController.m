//
//  MenuViewController.m
//  chatter
//
//  Created by Jim Greco on 11/4/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuViewController.h"

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
    }
    else {
        // Getting here implies that the user has done the tutorial
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        [[NSUserDefaults standardUserDefaults] setObject:userHandle forKey:@"userHandle"];
        ud.handle = userHandle;
    }
    
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

- (void) prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSString * segueName = segue.identifier;
    if ([segueName isEqualToString: @"menuContainerSegue"]) {
        UINavigationController* childViewController = (UINavigationController*)[segue destinationViewController];
        UITableViewController* menu = [childViewController.viewControllers objectAtIndex:0];
        UITableView* menuview = (UITableView*)menu.view;
        UITableViewCell* cell = [menuview cellForRowAtIndexPath:0];
        [menu performSegueWithIdentifier:@"chatroomSegue" sender:self];
    }
}

@end
