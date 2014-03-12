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


@interface ChatListViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, MKMapViewDelegate, UIBarPositioningDelegate>


@property (nonatomic,retain) IBOutlet UITableView* tableView;
@property (nonatomic,retain) IBOutlet MKMapView* mapView;
@property (nonatomic,retain) IBOutlet UIView* mapParentView;
@property (nonatomic,retain) IBOutlet UIView* tableParentView;

- (void)initCode;
- (IBAction)segmentedControlSwitched:(id)sender;
- (IBAction)unwindToChatList:(UIStoryboardSegue*)unwindSegue;

// Map View
- (IBAction)locateButtonPressed:(id)sender;


@end
