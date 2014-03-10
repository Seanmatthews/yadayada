//
//  Location.h
//  chatter
//
//  Created by sean matthews on 11/2/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import Foundation;
@import CoreLocation;


@interface Location : NSObject <CLLocationManagerDelegate>

@property (atomic) long long currentLat;
@property (atomic) long long currentLong;
@property (atomic) CLLocationCoordinate2D currentLocation;

- (id)init;
- (void)startServiceWithInterval:(NSTimeInterval)interval andDuration:(NSTimeInterval)duration;
- (void)stopService;
- (CGFloat)milesToCurrentLocationFrom:(CLLocationCoordinate2D)coords;
- (NSUInteger)metersToCurrentLocationFrom:(CLLocationCoordinate2D)coords;

+ (id)sharedInstance;
+ (double)fromLongLong:(long long)storedCoord;
+ (CLLocationCoordinate2D)fromLongLongLatitude:(long long)latitude Longitude:(long long)longitude;
+ (long long)toLongLong:(double)coord;
+ (CGFloat)milesBetweenSource:(CLLocationCoordinate2D)firstCoords andDestination:(CLLocationCoordinate2D)secondCoords;
+ (long long)metersFromMiles:(CGFloat)miles;

@end
