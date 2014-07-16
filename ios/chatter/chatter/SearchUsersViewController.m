//
//  SearchUsersViewController.m
//  chatter
//
//  Created by sean matthews on 6/8/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "SearchUsersViewController.h"
#import "Messages.h"
#import "Connection.h"
#import "Contacts.h"
#import "UserDetails.h"
#import "Location.h"
#import "SnarkyErrorMessages.h"

@interface SearchUsersViewController ()
{
    NSMutableArray *contentList;
    NSMutableArray *filteredContentList;
    BOOL isSearching;
}

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar;
- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchTe;
- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar;
- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar;
- (void)searchTableList;
- (void)receivedUserInfo:(NSNotification*)notification;
- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)inviteUser:(UserInfoMessage*)message;

@end

@implementation SearchUsersViewController

@synthesize inviteChatroom;

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
//    contentList = [[NSMutableArray alloc] init];
    contentList = [[NSMutableArray alloc] initWithObjects:@"iPhone", @"iPod", @"iPod touch", @"iMac", @"Mac Pro", @"iBook",@"MacBook", @"MacBook Pro", @"PowerBook", nil];
    filteredContentList = [[NSMutableArray alloc] init];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self registerForNotifications];
    
    // Send message request for user names & user ids
    SearchUsersMessage* sum = [[SearchUsersMessage alloc] init];
    sum.query = @"*";
    [[Connection sharedInstance] sendMessage:sum];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [self.view endEditing:YES];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self unregisterForNotifications];
}

-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar {
    // There are two bars in this view-- navigation and search.
    // If searchbar is given UIBarPositionTopAttached, it overlaps with the navigation bar.
    if ([bar isKindOfClass:[UINavigationBar class]]) {
        return UIBarPositionTopAttached;
    }
    else {
        return UIBarPositionAny;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(receivedUserInfo:)
                                                 name:@"UserInfoMessage"
                                               object:nil];
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark - UITableView delegate & data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if (isSearching) {
        return [filteredContentList count];
    }
    return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    // Configure the cell...
    if (isSearching) {
        cell.textLabel.text = [[filteredContentList objectAtIndex:indexPath.row] handle];
    }
    return cell;
    
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    UserInfoMessage* uim = [filteredContentList objectAtIndex:indexPath.row];
    [self inviteUser:uim];
    [self performSegueWithIdentifier:@"unwindToChatroom" sender:nil];
}

#pragma mark - Search Bar

- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    isSearching = YES;
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {
//    NSLog(@"Text change - %d",isSearching);
//    
//    //Remove all objects first.
//    [filteredContentList removeAllObjects];
//    
    if([searchText length] != 0) {
        isSearching = YES;
//        [self searchTableList];
    }
    else {
        isSearching = NO;
    }
//    [self.tblContentList reloadData];
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
//    NSLog(@"Cancel clicked");
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    if (_searchBar.text.length > 0) {
        [filteredContentList removeAllObjects];
        SearchUsersMessage* sum = [[SearchUsersMessage alloc] init];
        sum.query = [_searchBar.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
        [[Connection sharedInstance] sendMessage:sum];
    }
    [self.view endEditing:YES];
//    [self searchTableList];
}

# pragma mark - Search

- (void)searchTableList {
    NSString *searchString = _searchBar.text;
    
    for (NSString *tempStr in contentList) {
        NSComparisonResult result = [tempStr compare:searchString
                                             options:(NSCaseInsensitiveSearch|NSDiacriticInsensitiveSearch)
                                               range:NSMakeRange(0, [searchString length])];
        if (result == NSOrderedSame) {
            [filteredContentList addObject:tempStr];
        }
    }
}

#pragma mark - Message I/O

- (void)receivedUserInfo:(NSNotification*)notification
{
    NSSortDescriptor *handleDescriptor = [[NSSortDescriptor alloc] initWithKey:@""
                                                                     ascending:YES
                                                                    selector:@selector(localizedCaseInsensitiveCompare:)];
    [filteredContentList addObject:notification.object];
    [filteredContentList sortUsingDescriptors:@[handleDescriptor]];
    
    // TODO: should delete list and refill it-- user handles may have changed
    [self.tblContentList reloadData];
}

- (void)inviteUser:(UserInfoMessage*)message
{
    if (message.userId == [[UserDetails sharedInstance] userId]) {
        UIAlertView* alert = [[UIAlertView alloc] initWithTitle: @"Invite yourself?"
                                                        message:[[SnarkyErrorMessages sharedInstance] messageForInviteSelf]
                                                       delegate:nil
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
    }
    else {
        InviteUserMessage* ium = [[InviteUserMessage alloc] init];
        ium.senderId = [[UserDetails sharedInstance] userId];
        ium.senderHandle = [[UserDetails sharedInstance] handle];
        ium.recipientId = message.userId;
        ium.chatroomId = [inviteChatroom.cid longLongValue];
        ium.chatroomName = inviteChatroom.chatroomName;
        ium.chatroomRadius = [inviteChatroom.radius longLongValue];
        ium.chatroomLat = [Location toLongLong:inviteChatroom.origin.latitude];
        ium.chatroomLong = [Location toLongLong:inviteChatroom.origin.longitude];
        [[Connection sharedInstance] sendMessage:ium];
        // TODO: add invited user to contacts list
    }
}

@end
