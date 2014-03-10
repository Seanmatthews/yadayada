//
//  ContactsListViewController.m
//  chatter
//
//  Created by sean matthews on 2/22/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "ContactsListViewController.h"

@interface ContactsListViewController ()

@end

@implementation ContactsListViewController

- (void)initCode
{
    contacts = [Contacts sharedInstance];
}

// This is called whenever the view is loaded through storyboard segues
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
    _tableView.layer.cornerRadius = 5;
    _tableView.layer.masksToBounds = YES;
    [contacts getAllContacts];
	[_tableView reloadData];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [contacts.contactsList count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"ContactsListCell";
    UITableViewCell *cell = (UITableViewCell*) [_tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    
    NSString* fName = [[contacts contactsList][indexPath.row] getFirstName];
    NSString* lName = [[contacts contactsList][indexPath.row] getLastName];
    [cell.textLabel setText:[NSString stringWithFormat:@"%@ %@",fName,lName]];

    return cell;
}

//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
//{
//    NSString* title = @"Woops";
//    return title;
//}


#pragma mark - UITableViewDelegate methods

//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return 55.0;
//}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [contacts setInvitedContact:[contacts.contactsList objectAtIndex:indexPath.row]];
    [self performSegueWithIdentifier:@"unwindToChatroom" sender:nil];
}


@end
