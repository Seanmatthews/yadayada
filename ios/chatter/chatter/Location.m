//
//  Location.m
//  chatter
//
//  Created by sean matthews on 11/2/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Location.h"

@implementation Location

- (id)init
{
    self = [super init];
    if (self) {
        // Location services
        locationMeasurements = [[NSMutableArray alloc] init];
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
        // This is the most important property to set for the manager.
        // It ultimately determines how the manager will attempt to
        // acquire location and thus, the amount of power that
        // will be consumed.
        locationManager.desiredAccuracy = kCLLocationAccuracyBest;//[[setupInfo objectForKey:kSetupInfoKeyAccuracy] doubleValue];
        
        _currentLat = 0;
        _currentLong = 0;
        _currentLocation = CLLocationCoordinate2DMake([Location fromLongLong:_currentLat], [Location fromLongLong:_currentLong]);
        
        _sleepBetweenUpdateSec = 10;
    }
    return self;
}

+ (id)sharedInstance
{
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
        // Additional initialization can go here
    });
    return _sharedObject;
}

- (void)startService
{
    backgroundQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    timerSource = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, backgroundQueue);
    dispatch_source_set_timer(timerSource, dispatch_time(DISPATCH_TIME_NOW, 0), 10.0*NSEC_PER_SEC, 0*NSEC_PER_SEC);
    dispatch_source_set_event_handler(timerSource, ^{[self updateLocation];});
    dispatch_resume(timerSource);
    
}

static BOOL firstTimeLocation = YES;
- (void)updateLocation
{
    if (firstTimeLocation) {
        firstTimeLocation = NO;
        [locationManager startUpdatingLocation];
        sleep(2);
        [locationManager stopUpdatingLocation];
    }
    else {
        [locationManager startUpdatingLocation];
        sleep(_sleepBetweenUpdateSec);
        [locationManager stopUpdatingLocation];
    }
    NSLog(@"location : %f : %f, %f", [bestEffortAtLocation.timestamp timeIntervalSince1970],
          bestEffortAtLocation.coordinate.latitude, bestEffortAtLocation.coordinate.longitude);
    
    _currentLat = (long long)((bestEffortAtLocation.coordinate.latitude + 400.) * 1000000.);
    _currentLong = (long long)((bestEffortAtLocation.coordinate.longitude + 400.) * 1000000.);
    _currentLocation = CLLocationCoordinate2DMake([Location fromLongLong:_currentLat], [Location fromLongLong:_currentLong]);
    
    NSLog(@"Updating location %f, %f",[Location fromLongLong:_currentLat],[Location fromLongLong:_currentLong]);
}


#pragma mark - Utility functions

+ (double)fromLongLong:(long long)storedCoord
{
    return (((double)storedCoord/1000000.) - 400.);
}

+ (long long)toLongLong:(double)coord
{
    return (long long)((coord + 400.) * 1000000.);
}

+ (CLLocationCoordinate2D)fromLongLongLatitude:(long long)latitude Longitude:(long long)longitude
{
    CLLocationCoordinate2D ret;
    ret.latitude = [Location fromLongLong:latitude];
    ret.longitude = [Location fromLongLong:longitude];
    return ret;
}

+ (CGFloat)milesBetweenSource:(CLLocationCoordinate2D)firstCoords andDestination:(CLLocationCoordinate2D)secondCoords
{
    
    // this radius is in M //KM
    
    double nRadius = 1609.344; //6371;
    
    // Get the difference between our two points
    // then convert the difference into radians
    
    double nDLat = (firstCoords.latitude - secondCoords.latitude)* (M_PI/180);
    double nDLon = (firstCoords.longitude - secondCoords.longitude)* (M_PI/180);
    
    double nLat1 =  secondCoords.latitude * (M_PI/180);
    double nLat2 =  secondCoords.latitude * (M_PI/180);
    
    double nA = pow ( sin(nDLat/2), 2 ) + cos(nLat1) * cos(nLat2) * pow ( sin(nDLon/2), 2 );
    
    double nC = 2 * atan2( sqrt(nA), sqrt( 1 - nA ));
    
    double nD = nRadius * nC;
    
    return nD;
}

- (CGFloat)mileToCurrentLocationFrom:(CLLocationCoordinate2D)coords
{
    return [Location milesBetweenSource:_currentLocation andDestination:coords];
}

+ (long long)metersFromMiles:(CGFloat)miles
{
    return (long long)(1609.344 * miles);
}


#pragma mark - CoreLocation functions

/*
 * We want to get and store a location measurement that
 * meets the desired accuracy. For this example, we are
 * going to use horizontal accuracy as the deciding factor.
 * In other cases, you may wish to use vertical accuracy,
 * or both together.
 */
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    
    //NSLog(@"got new gps");
    //NSLog(@"got new gps location: %f, %f", newLocation.coordinate.latitude, newLocation.coordinate.longitude);
    
    // store all of the measurements, just so we can see what kind of data we might receive
    [locationMeasurements addObject:newLocation];
    // test the age of the location measurement to determine if the measurement is cached
    // in most cases you will not want to rely on cached measurements
    NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];
    if (locationAge > 5.0) return;
    // test that the horizontal accuracy does not indicate an invalid measurement
    if (newLocation.horizontalAccuracy < 0) return;
    // test the measurement to see if it is more accurate than the previous measurement
    if (bestEffortAtLocation == nil || bestEffortAtLocation.horizontalAccuracy > newLocation.horizontalAccuracy) {
        // store the location as the "best effort"
        bestEffortAtLocation = newLocation;
        // test the measurement to see if it meets the desired accuracy
        //
        // IMPORTANT!!! kCLLocationAccuracyBest should not be used for comparison with location coordinate or altitidue
        // accuracy because it is a negative value. Instead, compare against some predetermined "real" measure of
        // acceptable accuracy, or depend on the timeout to stop updating. This sample depends on the timeout.
        //
        if (newLocation.horizontalAccuracy <= locationManager.desiredAccuracy) {
            // we have a measurement that meets our requirements, so we can stop updating the location
            //
            // IMPORTANT!!! Minimize power usage by stopping the location manager as soon as possible.
            //
            [self stopUpdatingLocation:NSLocalizedString(@"Acquired Location", @"Acquired Location")];
            // we can also cancel our previous performSelector:withObject:afterDelay: - it's no longer necessary
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(stopUpdatingLocation:) object:nil];
        }
    }
}


- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    // The location "unknown" error simply means the manager
    // is currently unable to get the location.
    // We can ignore this error for the scenario of getting
    // a single location fix, because we already have a
    // timeout that will stop the location manager to save power.
    if ([error code] != kCLErrorLocationUnknown) {
        [self stopUpdatingLocation:NSLocalizedString(@"Error", @"Error")];
    }
}


- (void)stopUpdatingLocation:(NSString *)state {
    //self.stateString = state;
    
    NSLog(@"location manager failed with error: %@",state);
    
    [locationManager stopUpdatingLocation];
    locationManager.delegate = nil;
}

@end
