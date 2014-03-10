//
//  Location.m
//  chatter
//
//  Created by sean matthews on 11/2/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Location.h"


@implementation Location
{
    CLLocationManager *locationManager;
    NSMutableArray *locationMeasurements;
    CLLocation *bestEffortAtLocation;
    dispatch_queue_t backgroundQueue;
    dispatch_source_t timerSource;
}

static const double kDegreesToRadians = M_PI / 180.0;
static const double kRadiansToDegrees = 180.0 / M_PI;
static const double MILES_METERS = 1609.34;

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
        
        _currentLocation = CLLocationCoordinate2DMake([Location fromLongLong:_currentLat], [Location fromLongLong:_currentLong]);
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

- (void)startServiceWithInterval:(NSTimeInterval)interval andDuration:(NSTimeInterval)duration
{
    backgroundQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    timerSource = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, backgroundQueue);
    dispatch_source_set_timer(timerSource, dispatch_time(DISPATCH_TIME_NOW, 0), 10.0*NSEC_PER_SEC, 0*NSEC_PER_SEC);
    dispatch_source_set_event_handler(timerSource, ^{
        
        [locationManager startUpdatingLocation];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration * NSEC_PER_SEC), backgroundQueue, ^{
            [locationManager stopUpdatingLocation];
            [[NSNotificationCenter defaultCenter] postNotificationName:@"LocationUpdateNotification" object:self];
        });
    });
    dispatch_resume(timerSource);
}

- (void)stopService
{
    dispatch_suspend(timerSource);
}



#pragma mark - CoreLocation functions

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    _currentLocation = [(CLLocation*)[locations lastObject] coordinate];
    _currentLat = [Location toLongLong:_currentLocation.latitude];
    _currentLong = [Location toLongLong:_currentLocation.longitude];
}


- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    // The location "unknown" error simply means the manager
    // is currently unable to get the location.
    // We can ignore this error for the scenario of getting
    // a single location fix, because we already have a
    // timeout that will stop the location manager to save power.
    if ([error code] != kCLErrorLocationUnknown) {
        [self stopService];
    }
}


#pragma mark - Utility Class Methods

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
    
    double earthRadius = 6371.01; // Earth's radius in Kilometers
	
	// Get the difference between our two points then convert the difference into radians
	double nDLat = (firstCoords.latitude - secondCoords.latitude) * kDegreesToRadians;
	double nDLon = (firstCoords.longitude - secondCoords.longitude) * kDegreesToRadians;
	
	double fromLat =  secondCoords.latitude * kDegreesToRadians;
	double toLat =  firstCoords.latitude * kDegreesToRadians;
	
	double nA =	pow ( sin(nDLat/2), 2 ) + cos(fromLat) * cos(toLat) * pow ( sin(nDLon/2), 2 );
	
	double nC = 2 * atan2( sqrt(nA), sqrt( 1 - nA ));
	double nD = earthRadius * nC;
	
	return nD * 1000. / MILES_METERS; // Return our calculated distance in MILES
}

+ (long long)metersFromMiles:(CGFloat)miles
{
    return (long long)(MILES_METERS * miles);
}


#pragma mark - Utility Member Methods

- (CGFloat)milesToCurrentLocationFrom:(CLLocationCoordinate2D)coords
{
    return [Location milesBetweenSource:_currentLocation andDestination:coords];
}

- (NSUInteger)metersToCurrentLocationFrom:(CLLocationCoordinate2D)coords
{
    return [self milesToCurrentLocationFrom:coords] * MILES_METERS;
}


@end
