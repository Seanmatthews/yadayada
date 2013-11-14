//
//  Location.h
//  chatter
//
//  Created by sean matthews on 11/2/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@interface Location : NSObject <CLLocationManagerDelegate>
{
    CLLocationManager *locationManager;
    NSMutableArray *locationMeasurements;
    CLLocation *bestEffortAtLocation;
    dispatch_queue_t backgroundQueue;
    dispatch_source_t timerSource;
}

@property long long currentLat;
@property long long currentLong;
@property CLLocationCoordinate2D currentLocation;
@property int sleepBetweenUpdateSec;

- (id)init;
+ (id)sharedInstance;
- (void)startService;
- (void)updateLocation;
+ (double)fromLongLong:(long long)storedCoord;
+ (long long)toLongLong:(double)coord;
+ (CGFloat)milesBetweenSource:(CLLocationCoordinate2D)firstCoords andDestination:(CLLocationCoordinate2D)secondCoords;


@end
