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
#import "ViewController.h"
#import "ChatroomListCell.h"
#import "ChatPointAnnotation.h"
#import "SmartButton.h"
#import "SettingsViewController.h"


@interface ChatListViewController ()

@end

@implementation ChatListViewController

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    
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
    
    // Give the views rounded corners
    _mapView.layer.cornerRadius = 5;
    _mapView.layer.masksToBounds = YES;
    _tableView.layer.cornerRadius = 5;
    _tableView.layer.masksToBounds = YES;
    _tableParentView.layer.cornerRadius = 5;
    _tableParentView.layer.masksToBounds = YES;
    _mapParentView.layer.cornerRadius = 5;
    _mapParentView.layer.masksToBounds = YES;
    
    // Setup the refresh control
    UIRefreshControl *refreshControl = [[UIRefreshControl alloc] init];
    [refreshControl addTarget:self action:@selector(refreshTable:) forControlEvents:UIControlEventValueChanged];
    [self.tableView addSubview:refreshControl];
    
    // Center map on user location
    MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance([location currentLocation], 5000., 5000);
    [_mapView setRegion:region animated:YES];
    [_mapView setUserTrackingMode:MKUserTrackingModeFollow animated:YES];
    [_mapView setCenterCoordinate:[location currentLocation] animated:YES];
}

- (void)viewWillAppear:(BOOL)animated
{
//    viewIsVisible = YES;
//    [self refreshTable:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
    viewIsVisible = NO;
}

- (void)viewDidAppear:(BOOL)animated
{
    viewIsVisible = YES;
    [self refreshTable:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - UI Behaviors

- (IBAction)segmentedControlSwitched:(id)sender
{
    UISegmentedControl* ctrl = (UISegmentedControl*)sender;
    if (0 == [ctrl selectedSegmentIndex]) {
        [_mapParentView setHidden:YES];
        [_tableParentView setHidden:NO];
    }
    else if (1 == [ctrl selectedSegmentIndex]) {
        [_tableParentView setHidden:YES];
        [_mapParentView setHidden:NO];
    }
}

- (IBAction)locateButtonPressed:(id)sender
{
    [_mapView setCenterCoordinate:[location currentLocation] animated:YES];
}


#pragma mark - Convenience functions

- (BOOL)canJoinChatroom:(ChatroomMessage*)chatroom
{
    CLLocationCoordinate2D chatroomOrigin = CLLocationCoordinate2DMake([Location fromLongLong:chatroom.latitude], [Location fromLongLong:chatroom.longitude]);
    NSUInteger distance = [location metersToCurrentLocationFrom:chatroomOrigin];
    
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
    msg.onlyJoinable = YES;
    msg.metersFromCoords = 0;
    [connection sendMessage:msg];
}

- (void)joinChatroomFromMap:(id)sender
{
    //    MKPinAnnotationView* mkp = (MKPinAnnotationView*)(((SmartButton*)sender).parent);
    //    ChatPointAnnotation* cpa = (ChatPointAnnotation*)mkp.annotation;
    ChatPointAnnotation* cpa = (ChatPointAnnotation*)(((SmartButton*)sender).parent);
    [self joinChatroomWithId:cpa.chatroomId];
    [_mapView deselectAnnotation:cpa animated:NO];
}

- (void)joinChatroomWithId:(long long)chatId
{
    if ([chatManager currentChatroomId] == chatId) {
        
        // This is a bad solution.
        // TODO: make this better
        
        JoinedChatroomMessage* msg = [[JoinedChatroomMessage alloc] init];
        msg.chatroomId = [chatManager currentChatroomId];
        msg.chatroomName = [chatManager currentChatroomName];
        msg.userId = ud.userId;
        msg.userHandle = ud.handle;
        [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:msg];
    }
    else {
        // First, leave our current chatroom, if any
        [self leaveCurrentChatroom];
        
        JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
        msg.chatroomId = chatId;
        msg.userId = ud.userId;
        msg.latitude = [location currentLat];
        msg.longitude = [location currentLong];
        [connection sendMessage:msg];
    }
}

- (void)leaveCurrentChatroom
{
    if ([chatManager.joinedChatrooms count] > 0) {
        LeaveChatroomMessage* msg = [[LeaveChatroomMessage alloc] init];
        msg.userId = ud.userId;
        msg.chatroomId = [chatManager currentChatroomId];
        NSLog(@"Leaving chatroom %lld",msg.chatroomId);
        [connection sendMessage:msg];
    }
}


#pragma mark - incoming and outgoing messages

- (void)messageCallback:(MessageBase*)message
{
    if (viewIsVisible) {
        UIAlertView *alert;
        switch (message.type) {
            case Chatroom:
                NSLog(@"chatroom");
                
                // Table
                [self addChatroom:(ChatroomMessage*)message];
                [_tableView reloadData];
                
                // Map
                [self addChatroomAnnotation:(ChatroomMessage*)message];
                break;
                
            case JoinedChatroom:
                NSLog(@"Joined Chatroom");
                if (((JoinedChatroomMessage*)message).userId == ud.userId) {
                    [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:message];
                }
                break;
                
            case JoinChatroomReject:
                NSLog(@"Could not join chatroom");
                alert = [[UIAlertView alloc] initWithTitle:@"Woops!" message:((JoinChatroomRejectMessage*)message).reason delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                [alert show];
                break;
                
            case LeftChatroom:
                NSLog(@"Left Chatroom");
                break;
        }
    }
}


#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"segue %@",segue.identifier);
    if ([segue.identifier isEqualToString:@"pickedChatroomSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatId = ((JoinedChatroomMessage*)sender).chatroomId; //tappedCellInfo.chatroomId;
        vc.chatTitle = ((JoinedChatroomMessage*)sender).chatroomName; //tappedCellInfo.chatroomName;
    }
    else if ([segue.identifier isEqualToString:@"createChatSegue"]) {

    }
    else if ([segue.identifier isEqualToString:@"chatList2SettingsSegue"]) {
        SettingsViewController* svc = (SettingsViewController*)segue.destinationViewController;
        svc.unwindSegueName = @"unwindToChatList";
    }
}

// For unwinding to this view.
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
    
//    if (indexPath.section == 0 && indexPath.row > [localChatroomList count]-1) {
//        return nil;
//    }
//    if (indexPath.section == 1 && indexPath.row > [globalChatroomList count]-1) {
//        return nil;
//    }
    
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
    
    if (chatroom.chatActivity > 100) {
        chatroom.chatActivity = 0;
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

    [self joinChatroomWithId:tappedCellInfo.chatroomId];
}


#pragma mark - Refresh Control

- (void)refreshTable:(UIRefreshControl*)refreshControl
{
    [localChatroomList removeAllObjects];
    [globalChatroomList removeAllObjects];
    [_tableView reloadData];
    [self searchChatrooms];
    if (refreshControl) {
        [refreshControl endRefreshing];
    }
}


#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    [mapView removeAnnotations:mapView.annotations];
    SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    msg.onlyJoinable = YES;
    msg.metersFromCoords = 1609.34 * 5.; // TODO: change to screen region bounds
    [connection sendMessage:msg];
}

- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id < MKAnnotation >)annotation
{
    MKPinAnnotationView* annotationView = nil;
    if ([annotation isKindOfClass:[ChatPointAnnotation class]]) {
        annotationView = (MKPinAnnotationView*)[mapView dequeueReusableAnnotationViewWithIdentifier:@"AnnotationView"];
        if (annotationView) {
            annotationView.annotation = annotation;
        }
        else {
            annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"AnnotationView"];
        }
        annotationView.canShowCallout = YES;
        SmartButton *disclosureButton = [SmartButton buttonWithType:UIButtonTypeContactAdd];
        disclosureButton.parent = annotationView.annotation;
        [disclosureButton addTarget:self action:@selector(joinChatroomFromMap:) forControlEvents:UIControlEventTouchUpInside];
        annotationView.rightCalloutAccessoryView = disclosureButton;
        return annotationView;
    }
    return annotationView;
}


#pragma mark - Map Annotations

- (void)addChatroomAnnotation:(ChatroomMessage*)message
{
    ChatPointAnnotation* mpa = [[ChatPointAnnotation alloc] init];
    CLLocationCoordinate2D coord;
    coord = [Location fromLongLongLatitude:message.latitude Longitude:message.longitude];
    
    // Need this check because in case of bad lat/long data.
    // iOS throws a nondescript deallocation error if you try to draw out of bounds lat/long annotations.
    if (coord.latitude >= -90. && coord.latitude <=90. && coord.longitude >= -180. && coord.longitude <= 180.) {
        NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
        [format setNumberStyle:NSNumberFormatterDecimalStyle];
        [format setMaximumFractionDigits:2];
        
        mpa.chatroomId = message.chatroomId;
        mpa.coordinate = coord;
        mpa.title = message.chatroomName;
        CGFloat milesToChat = [location milesToCurrentLocationFrom:mpa.coordinate];
        mpa.subtitle = [NSString stringWithFormat:@"%@miles  %dusers  %d%%",[format stringFromNumber:[NSNumber numberWithFloat:milesToChat]],message.userCount,message.chatActivity];
        [_mapView addAnnotation:mpa];
    }
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)view calloutAccessoryControlTapped:(UIControl*)control
{
    NSLog(@"calloutAccessoryControlTapped");
    [self performSegueWithIdentifier:@"ChatAnnotation" sender:view];
}

- (void)deselectAllAnnotations
{
    NSArray *selectedAnnotations = _mapView.selectedAnnotations;
    for(id annotationView in selectedAnnotations) {
        if ([annotationView isSelected]) {
            [_mapView deselectAnnotation:[annotationView annotation] animated:NO];
        }
    }
}


@end
