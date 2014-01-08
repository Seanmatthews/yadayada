//
//  MapViewController.h
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

@interface MapViewController : UIViewController <MKMapViewDelegate>
{
    Location* location;
    Connection* connection;
    BOOL viewIsVisible;
}

@property (nonatomic,retain) IBOutlet MKMapView* mapView;

- (void)initCode;
- (void)messageCallback:(MessageBase*)message;
- (void)addChatroomAnnotation:(ChatroomMessage*)message;
- (IBAction)locateButtonPressed:(id)sender;


@end
