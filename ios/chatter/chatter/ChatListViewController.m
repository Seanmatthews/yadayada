//
//  ChatListViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ChatListViewController.h"
#import "UIImage+ImageEffects.h"
#import "MenuViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "ViewController.h"
#import "ChatroomListCell.h"
#import "CreateChatViewController.h"

@interface ChatListViewController ()

@end

@implementation ChatListViewController

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    
    localChatroomList = [[NSMutableArray alloc] init];
    globalChatroomList = [[NSMutableArray alloc] init];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    ChatListViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
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
	
    // Give the map view rounded corners
    _tableView.layer.cornerRadius = 5;
    _tableView.layer.masksToBounds = YES;
    
    // Setup the refresh control
    UIRefreshControl *refreshControl = [[UIRefreshControl alloc] init];
    [refreshControl addTarget:self action:@selector(refreshTable:) forControlEvents:UIControlEventValueChanged];
    [self.tableView addSubview:refreshControl];
}

- (void)viewWillAppear:(BOOL)animated
{
    // Send a chatroom search message.
    [self searchChatrooms];
}

- (void)viewWillDisappear:(BOOL)animated
{
    if ([self isBeingDismissed]) {
        [connection removeCallbackBlockFromSender:NSStringFromClass([self class])];
    }
}

static BOOL onceToChatView = YES;
- (void)viewDidAppear:(BOOL)animated
{
//    if (onceToChatView) {
//        onceToChatView = NO;
//        JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
//        msg.userId = ud.userId;
//        msg.chatroomId = ud.chatroomId;
//        msg.latitude = 0;
//        msg.longitude = 0;
//        [connection sendMessage:msg];
//    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)canJoinChatroom:(ChatroomMessage*)chatroom
{
    CLLocationCoordinate2D chatroomOrigin = CLLocationCoordinate2DMake([Location fromLongLong:chatroom.latitude], [Location fromLongLong:chatroom.longitude]);
    CGFloat distance = [location milesToCurrentLocationFrom:chatroomOrigin];
    
    // Only display local chatrooms that the user is able to join
    if (distance - chatroom.radius > 0) {
        NSLog(@"Chatroom is too far away");
        return NO;
    }
    return YES;
}

- (void)addChatroom:(ChatroomMessage*)chatroom
{
    // Determine whether chatroom is local or global
    if (chatroom.radius <= 0) {
        [globalChatroomList addObject:chatroom];
    }
    else {
        // Are we close enough to join this chatroom?
        if ([self canJoinChatroom:chatroom]) {
            [localChatroomList addObject:chatroom];
        }
    }
    [_tableView reloadData];
}

- (void)searchChatrooms
{
    SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}


#pragma mark - incoming and outgoing messages


- (void)messageCallback:(MessageBase*)message
{
    UIAlertView *alert;
    switch (message.type) {
        case Chatroom:
            [self addChatroom:(ChatroomMessage*)message];
            [_tableView reloadData];
            break;
            
        case JoinedChatroom:
            NSLog(@"Joined Chatroom");
            [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:nil];
            break;
            
        case JoinChatroomReject:
            NSLog(@"Could not join chatroom");
            alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:((JoinChatroomRejectMessage*)message).reason delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alert show];
            break;
            
        case LeftChatroom:
            NSLog(@"Left Chatroom");
            break;
    }
}


#pragma mark - Blurred Snapshot & Segue

- (UIImage*)blurredSnapshot
{
    // Create the image context
    UIGraphicsBeginImageContextWithOptions(self.view.bounds.size, YES, self.view.window.screen.scale);
    
    // There he is! The new API method
    [self.view drawViewHierarchyInRect:self.view.frame afterScreenUpdates:NO];
    
    // Get the snapshot
    UIImage *snapshotImage = UIGraphicsGetImageFromCurrentImageContext();
    //[UIImage imageNamed:@"Default@2x.png"];
    
    // Now apply the blur effect using Apple's UIImageEffect category
    UIImage *blurredSnapshotImage = [snapshotImage applyLightEffect];
    
    // Be nice and clean your mess up
    UIGraphicsEndImageContext();
    
    return blurredSnapshotImage;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"pickedChatroomSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatId = tappedCellInfo.chatroomId;
        vc.chatTitle = tappedCellInfo.chatroomName;
    }
    else if ([segue.identifier isEqualToString:@"chatList2CreateChatSegue"]) {
        CreateChatViewController* ccvc = (CreateChatViewController*)segue.destinationViewController;
        ccvc.unwindSegueName = @"unwindToChatList";
    }
    else {
        MenuViewController* vc = (MenuViewController*)segue.destinationViewController;
        vc.image =[self blurredSnapshot];
    }
}

- (IBAction)unwindToChatList:(UIStoryboardSegue*)unwindSegue
{
    
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Globals and Locals
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    int count = 0;
    if (section == 0) {
        count = [localChatroomList count];
    }
    else if (section == 1) {
        count = [globalChatroomList count];
    }
    return count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *CellIdentifier = @"ChatroomListCell";
    ChatroomListCell *cell = (ChatroomListCell*) [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        NSArray* topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ChatroomListCell" owner:self options:nil];
        for (id currentObject in topLevelObjects) {
            if ([currentObject isKindOfClass:[UITableViewCell class]]) {
                cell = (ChatroomListCell*)currentObject;
                break;
            }
        }
    }
    
    // Configure the cell based on whether chatroom is global or local
    NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
    [format setNumberStyle:NSNumberFormatterDecimalStyle];
    [format setMaximumFractionDigits:2];
    ChatroomMessage *chatroom;
    NSString* distanceStr = @"";
    
    if (indexPath.section == 0) { // Local chats
        chatroom = [localChatroomList objectAtIndex:indexPath.row];
        CLLocationCoordinate2D chatroomOrigin = CLLocationCoordinate2DMake([Location fromLongLong:chatroom.latitude], [Location fromLongLong:chatroom.longitude]);
        CGFloat distance = [location milesToCurrentLocationFrom:chatroomOrigin];
        distanceStr = [format stringFromNumber:[NSNumber numberWithFloat:distance]];
    }
    else if (indexPath.section == 1) {
        chatroom = [globalChatroomList objectAtIndex:indexPath.row];
        distanceStr = @"na";
    }
    
    // TODO: get an image
    cell.chatroomImage.image = [[UIImage alloc] init];
    cell.chatroomName.text = chatroom.chatroomName;
    cell.milesFromOrigin.text = [NSString stringWithFormat:@"%@ miles away",distanceStr];
    NSString* activityStr = [format stringFromNumber:[NSNumber numberWithShort:chatroom.chatActivity]];
    cell.percentActive.text = [NSString stringWithFormat:@"%@%% active",activityStr];
    NSString* userStr = [format stringFromNumber:[NSNumber numberWithInt:chatroom.userCount]];
    cell.numberOfUsers.text = [NSString stringWithFormat:@"%@ users",userStr];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString* title = @"Woops";
    if (section == 0) {
        title = @"Local Chats";
    }
    else if (section == 1) {
        title = @"Global Chats";
    }
    return title;
}

#pragma mark - UITableViewDelegate methods

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 55.0;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0) {
        tappedCellInfo = (ChatroomMessage*)[localChatroomList objectAtIndex:indexPath.row];
    }
    else if (indexPath.section == 1) {
        tappedCellInfo = (ChatroomMessage*)[globalChatroomList objectAtIndex:indexPath.row];
    }

    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
    msg.chatroomId = tappedCellInfo.chatroomId;
    msg.userId = ud.userId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}


#pragma mark - Refresh Control

- (void)refreshTable:(UIRefreshControl*)refreshControl
{
    [localChatroomList removeAllObjects];
    [globalChatroomList removeAllObjects];
    [self searchChatrooms];
    [refreshControl endRefreshing];
}


@end
