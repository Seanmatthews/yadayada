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

@interface MapViewController : UIViewController <MKMapViewDelegate>
{
    Location* location;
}

@property (nonatomic,retain) IBOutlet MKMapView* mapView;

@end
