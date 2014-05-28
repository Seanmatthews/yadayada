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
//- (void)receivedChatroom:(NSNotification*)notification;
- (void)refreshTable:(UIRefreshControl*)refreshControl;
- (void)segueToChatroom:(NSNotification*)notification;

// Map view
//- (void)addChatroomAnnotation:(ChatroomMessage*)message;
- (void)addAnnotationForChatroom:(Chatroom*)chatroom;
- (void)removeAnnotationForChatroom:(Chatroom*)chatroom;
- (void)joinChatroomFromMap:(id)sender;
- (void)deselectAllAnnotations;
- (void)reloadMapAnnotations;
- (void)adjustMapAnnotationsFromChatArray:(NSArray*)oldChatArray;

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
    // KVOs so we know when to reload table/map
    [chatManager addObserver:self
                  forKeyPath:@"joinedChatrooms"
                     options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld)
                     context:nil];
    
    [chatManager addObserver:self
                  forKeyPath:@"localChatrooms"
                     options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld)
                     context:nil];
    
    [chatManager addObserver:self
                  forKeyPath:@"globalChatrooms"
                     options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld)
                     context:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(segueToChatroom:)
                                                 name:@"segueToChatroomNotification"
                                               object:nil];
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [chatManager removeObserver:self forKeyPath:@"joinedChatrooms"];
    [chatManager removeObserver:self forKeyPath:@"localChatrooms"];
    [chatManager removeObserver:self forKeyPath:@"globalChatrooms"];
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
    [self reloadMapAnnotations];
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


#pragma mark - Notifications

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    NSArray* oldChatList = [change objectForKey:@"localChatrooms"];
    if ([keyPath isEqual:@"joinedChatrooms"]) {
        
    }
    else if ([keyPath isEqual:@"localChatrooms"]) {
        // TODO this may be faster
        [self adjustMapAnnotationsFromChatArray:oldChatList];
//        [self reloadMapAnnotations];
    }
    else if ([keyPath isEqual:@"globalChatrooms"]) {
        // TODO this may be faster
        [self adjustMapAnnotationsFromChatArray:oldChatList];
//        [self reloadMapAnnotations];
    }
    [_tableView reloadData];
//    [self reloadMapAnnotations];
}

- (void)adjustMapAnnotationsFromChatArray:(NSArray*)oldChatArray
{
    // apply only the change
    if (oldChatArray.count < chatManager.localChatrooms.count) {
        [self addAnnotationForChatroom:chatManager.localChatrooms.lastObject];
    }
    else if (oldChatArray.count > chatManager.localChatrooms.count) {
        // TODO
//        [self removeAnnotationForChatroom:oldChatArray.lastObject];
    }
}


#pragma mark - Convenience Methods

- (void)joinChatroomFromMap:(id)sender
{
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
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
        cell.selectionStyle = UITableViewCellSelectionStyleGray;
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
    
    NSString* activityStr = [format stringFromNumber:chatroom.chatActivity];
    NSString* userStr = [format stringFromNumber:chatroom.userCount];
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
    
    [chatManager joinChatroom:tappedCellInfo withCompletion:^{
        [self performSegueWithIdentifier:@"pickedChatroomSegue" sender:tappedCellInfo];
    }];
}


#pragma mark - Refresh Control

- (void)refreshTable:(UIRefreshControl*)refreshControl
{
    [chatManager searchChatrooms]; // TODO: move this function to chat manager
    if (refreshControl) {
        [refreshControl endRefreshing];
    }
    [_tableView reloadData];
    [self reloadMapAnnotations];
}


#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{

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

- (void)addAnnotationForChatroom:(Chatroom*)chatroom
{
    ChatPointAnnotation* mpa = [[ChatPointAnnotation alloc] init];
    CGFloat milesToChat = [location milesToCurrentLocationFrom:chatroom.origin];
    NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
    [format setNumberStyle:NSNumberFormatterDecimalStyle];
    [format setMaximumFractionDigits:2];
    
    mpa.chatroomId = [chatroom.cid longLongValue];
    mpa.coordinate = chatroom.origin;
    mpa.title = chatroom.chatroomName;
    mpa.subtitle = [NSString stringWithFormat:@"%@miles  %dusers  %d%%",
                    [format stringFromNumber:[NSNumber numberWithFloat:milesToChat]],
                    [chatroom.userCount shortValue],[chatroom.chatActivity shortValue]];
    [_mapView addAnnotation:mpa];
}

- (void)removeAnnotationForChatroom:(Chatroom*)chatroom
{
    // TODO: If this requires the exact object, we'll need to keep a dict of added annotations
}

- (void)reloadMapAnnotations
{
    [_mapView removeAnnotations:_mapView.annotations];
    for (Chatroom* c in chatManager.chatrooms.allValues) {
        [self addAnnotationForChatroom:c];
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
