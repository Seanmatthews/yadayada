//
//  ChatListViewController.h
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>
#import "Location.h"
#import "Messages.h"
#import "Connection.h"
#import "UserDetails.h"
#import "ChatroomManagement.h"


@interface ChatListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, MKMapViewDelegate>
{
    Location* location;
    Connection* connection;
    UserDetails* ud;
//    NSMutableArray* recentChatroomList;
    NSMutableArray* localChatroomList;
    NSMutableArray* globalChatroomList;
    ChatroomMessage *tappedCellInfo;
    BOOL viewIsVisible;
    ChatroomManagement* chatManager;
}

@property (nonatomic,retain) IBOutlet UITableView* tableView;
@property (nonatomic,retain) IBOutlet MKMapView* mapView;
@property (nonatomic,retain) IBOutlet UIView* mapParentView;
@property (nonatomic,retain) IBOutlet UIView* tableParentView;
@property (atomic, retain) NSMutableArray* recentChatroomList;

- (void)initCode;
- (void)messageCallback:(MessageBase*)message;
- (IBAction)segmentedControlSwitched:(id)sender;
- (IBAction)unwindToChatList:(UIStoryboardSegue*)unwindSegue;
- (void)leaveCurrentChatroom;
- (void)addRecentChatroom:(JoinedChatroomMessage*)chatroom;
- (BOOL)canJoinChatroomWithCoord:(CLLocationCoordinate2D)coord andRadius:(long long)radius;

// Table view
- (void)refreshTable:(UIRefreshControl*)refreshControl;
- (void)searchChatrooms;
- (BOOL)canJoinChatroom:(ChatroomMessage*)chatroom;

// Map View
- (void)addChatroomAnnotation:(ChatroomMessage*)message;
- (void)joinChatroomFromMap:(id)sender;
- (void)joinChatroomWithId:(long long)chatId;
- (void)deselectAllAnnotations;
- (IBAction)locateButtonPressed:(id)sender;


@end
