//
//  MenuTableViewController.m
//  chatter
//
//  Created by sean matthews on 11/11/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MenuTableViewController.h"
#import "CreateChatViewController.h"

@interface MenuTableViewController ()

@end

@implementation MenuTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    
}

- (void)viewDidAppear:(BOOL)animated
{

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"createChatroomSegue"]) {
        CreateChatViewController* ccvc = (CreateChatViewController*)segue.destinationViewController;
        ccvc.unwindSegueName = @"unwindToMenu";
    }
}

@end
