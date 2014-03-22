//
//  ChatListViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import QuartzCore;
#import "ChatListViewController.h"
#import "MenuViewController.h"
#import "ViewController.h"
#import "ChatroomListCell.h"
#import "ChatPointAnnotation.h"
#import "SmartButton.h"
#import "SettingsViewController.h"
#import "CreateChatViewController.h"


@interface ChatListViewController ()

- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)receivedChatroom:(NSNotification*)notification;
- (BOOL)canJoinChatroom:(Chatroom*)chatroom;
- (void)refreshTable:(UIRefreshControl*)refreshControl;
- (void)segueToChatroom:(NSNotification*)notification;

// Map view
- (void)addChatroomAnnotation:(ChatroomMessage*)message;
- (void)joinChatroomFromMap:(id)sender;
- (void)deselectAllAnnotations;
- (void)reloadMapAnnotations;

@end

@implementation ChatListViewController
{
    Location* location;
    Connection* connection;
    UserDetails* ud;
    Chatroom *tappedCellInfo;
    ChatroomManagement* chatManager;
}


const int MAX_RECENT_CHATS = 5;

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    [_mapParentView setHidden:YES];
    connection = [Connection sharedInstance];
}

- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
}

- (void)registerForNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(segueToChatroom:)
                                                 name:@"segueToChatroomNotification"
                                               object:nil];
    
    for (NSString* notificationName in @[@"JoinChatroomReject", @"Chatroom"]) {
        NSString* selectorName = [NSString stringWithFormat:@"received%@:",notificationName];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:NSSelectorFromString(selectorName)
                                                     name:[NSString stringWithFormat:@"%@Message",notificationName]
                                                   object:nil];
    }
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Give the views rounded corners
    _mapView.layer.cornerRadius = 5;
    _mapView.layer.masksToBounds = YES;
//    _tableView.layer.cornerRadius = 5;
//    _tableView.layer.masksToBounds = YES;
    _tableView.allowsSelection = YES;
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
    [super viewWillAppear:animated];
    [self registerForNotifications];
    [self refreshTable:nil];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self unregisterForNotifications];
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
    [self refreshTable:nil];
}

- (IBAction)locateButtonPressed:(id)sender
{
    [_mapView setCenterCoordinate:[location currentLocation] animated:YES];
}

-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar {
    return UIBarPositionTopAttached;
}


#pragma mark - Convenience functions


- (BOOL)canJoinChatroom:(Chatroom*)chatroom
{
    NSUInteger distance = [location metersToCurrentLocationFrom:chatroom.origin];
    
    // Only display local chatrooms that the user is able to join
    if (distance - [chatroom.radius longLongValue] > 0) {
        NSLog(@"[chat list view] Chatroom is too far away");
        return NO;
    }
    return YES;
}


#pragma mark - incoming and outgoing messages

- (void)receivedChatroom:(NSNotification*)notification
{
    if (!_tableParentView.isHidden) {
        [_tableView reloadData];
    }
    
    // TODO: map should be grabbing data from chat manager?
    // Should we display rooms that are too far away?
    if (!_mapParentView.isHidden) {
        [self addChatroomAnnotation:notification.object];
        [self reloadMapAnnotations];
    }
}


#pragma mark - Convenience Methods

- (void)joinChatroomFromMap:(id)sender
{
    //    MKPinAnnotationView* mkp = (MKPinAnnotationView*)(((SmartButton*)sender).parent);
    //    ChatPointAnnotation* cpa = (ChatPointAnnotation*)mkp.annotation;
    ChatPointAnnotation* cpa = (ChatPointAnnotation*)(((SmartButton*)sender).parent);
    [_mapView deselectAnnotation:cpa animated:NO];
    Chatroom* c = [chatManager.chatrooms objectForKey:[NSNumber numberWithLongLong:cpa.chatroomId]];
    [chatManager joinChatroom:c
               withCompletion:^{
                   [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:c];
               }];
}


#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"segue %@",segue.identifier);
    if ([segue.identifier isEqualToString:@"pickedChatroomSegue"]) {
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatroom = sender;
    }
}

// For unwinding to this view.
- (IBAction)unwindToChatList:(UIStoryboardSegue*)unwindSegue
{

}

- (void)segueToChatroom:(NSNotification*)notification
{
    NSLog(@"[chat list view] Going to segue");
    [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:notification.object];
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Recents, Locals, Globals
    return 3;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSUInteger count = 0;
    if (section == 0) {
        count = [chatManager.joinedChatrooms count];
    }
    else if (section == 1) {
        count = [chatManager.localChatrooms count];
    }
    else if (section == 2) {
        count = [chatManager.globalChatrooms count];
    }
    return count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"ChatroomListCell";
//    ChatroomListCell *cell = (ChatroomListCell*) [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
//        NSArray* topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ChatroomListCell" owner:self options:nil];
//        for (id currentObject in topLevelObjects) {
//            if ([currentObject isKindOfClass:[UITableViewCell class]]) {
//                cell = (ChatroomListCell*)currentObject;
//                cell.selectionStyle = UITableViewCellSelectionStyleGray;
//                break;
//            }
//        }
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
        cell.selectionStyle = UITableViewCellSelectionStyleGray;
//        cell.layer.borderWidth = 1.;
//        cell.layer.borderColor = [UIColor lightGrayColor].CGColor;
    }
    
    // Configure the cell based on whether chatroom is global or local
    NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
    [format setNumberStyle:NSNumberFormatterDecimalStyle];
    [format setMaximumFractionDigits:2];
    Chatroom *chatroom;
    NSString* distanceStr = @"";
    
    if (indexPath.section == 0) {
        chatroom = [[chatManager joinedChatrooms] objectAtIndex:indexPath.row];
    }
    else if (indexPath.section == 1) {
        chatroom = [[chatManager localChatrooms] objectAtIndex:indexPath.row];
    }
    else if (indexPath.section == 2) {
        chatroom = [[chatManager globalChatrooms] objectAtIndex:indexPath.row];
    }
    
    // Calculate distance from chat, even global
    CGFloat distance = [location milesToCurrentLocationFrom:chatroom.origin];
    distanceStr = [format stringFromNumber:[NSNumber numberWithFloat:distance]];
    
    if ([chatroom.chatActivity shortValue] > 100) {
        chatroom.chatActivity = @0;
    }
    
    // TODO: get an image
//    cell.chatroomImage.image = [[UIImage alloc] init];
//    cell.chatroomName.text = chatroom.chatroomName;
//    cell.milesFromOrigin.text = [NSString stringWithFormat:@"%@ miles away",distanceStr];
    NSString* activityStr = [format stringFromNumber:chatroom.chatActivity];
//    cell.percentActive.text = [NSString stringWithFormat:@"%@%% active",activityStr];
    NSString* userStr = [format stringFromNumber:chatroom.userCount];
//    cell.numberOfUsers.text = [NSString stringWithFormat:@"%@ users",userStr];
//    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%@ miles away  %@%% active  %@ users", distanceStr, activityStr, userStr];
    cell.textLabel.text = chatroom.chatroomName;
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString* title = @"Woops";
    if (section == 0) {
        title = @"Recent";
    }
    else if (section == 1) {
        title = @"Local";
    }
    else if (section == 2) {
        title = @"Global";
    }
    return title;
}


#pragma mark - UITableViewDelegate methods

-(UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
//    _tableView.clipsToBounds = NO;
//    _tableView.layer.masksToBounds = NO;
//    [_tableView.layer setShadowColor:[[UIColor lightGrayColor] CGColor]];
//    [_tableView.layer setShadowOffset:CGSizeMake(0, 0)];
//    [_tableView.layer setShadowRadius:5.0];
//    [_tableView.layer setShadowOpacity:1];
    
    UILabel *label = [[UILabel alloc] init];
    label.text = [self tableView:tableView titleForHeaderInSection:section];
    label.backgroundColor = [UIColor colorWithRed:0.9 green:0.9 blue:0.9 alpha:1.];
    label.textAlignment = NSTextAlignmentLeft;
    label.textColor = [UIColor grayColor];
//    [label.layer setShadowColor:[[UIColor grayColor] CGColor]];
//    [label.layer setShadowOffset:CGSizeMake(0, 0)];
//    [label.layer setShadowRadius:3.0];
//    [label.layer setShadowOpacity:1];
    
    return label;
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 25;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 55.0;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0) {
        tappedCellInfo = (Chatroom*)[[chatManager joinedChatrooms] objectAtIndex:indexPath.row];
    }
    else if (indexPath.section == 1) {
        tappedCellInfo = (Chatroom*)[[chatManager localChatrooms] objectAtIndex:indexPath.row];
    }
    else if (indexPath.section == 2) {
        tappedCellInfo = (Chatroom*)[[chatManager globalChatrooms] objectAtIndex:indexPath.row];
    }
    
//    if ([chatManager alreadyJoinedChatroom:tappedCellInfo]){
//        [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:tappedCellInfo];
//    }
//    else {
        [chatManager joinChatroom:tappedCellInfo withCompletion:^{
            [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:tappedCellInfo];
        }];
//    }
}


#pragma mark - Refresh Control

- (void)refreshTable:(UIRefreshControl*)refreshControl
{
//    [_tableView reloadData];
    [chatManager searchChatrooms]; // TODO: move this function to chat manager
    if (refreshControl) {
        [refreshControl endRefreshing];
    }
    NSLog(@"%lu",(unsigned long)[[chatManager globalChatrooms] count]);
    [_tableView reloadData];
}


#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    if (!_mapParentView.isHidden) {
        
        // TODO: call from chatmanager
        NSLog(@"region changed");
        [mapView removeAnnotations:mapView.annotations];
        SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
        msg.latitude = [location currentLat];
        msg.longitude = [location currentLong];
        msg.onlyJoinable = YES;
        msg.metersFromCoords = 1609.34 * 5.; // TODO: change to screen region bounds
        [connection sendMessage:msg];
    }
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

- (void)reloadMapAnnotations
{
    // Figure out our current region
    
    // Determine which chatrooms haven't been placed
    
    // Place them
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
