//
//  MapViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MapViewController.h"
#import "UIImage+ImageEffects.h"
#import "MenuViewController.h"
#import <QuartzCore/QuartzCore.h>


@interface MapViewController ()

@end

@implementation MapViewController

- (void)initCode
{
    location = [Location sharedInstance];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    MapViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
}

- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Give the map view rounded corners
    _mapView.layer.cornerRadius = 5;
    _mapView.layer.masksToBounds = YES;
}

- (void)viewWillAppear:(BOOL)animated
{
    MKCoordinateRegion region = MKCoordinateRegionMakeWithDistance([location currentLocation], 5000., 5000);
    [_mapView setRegion:region animated:YES];
    [_mapView setUserTrackingMode:MKUserTrackingModeFollow animated:YES];
    [_mapView setCenterCoordinate:[location currentLocation] animated:YES];
}

- (void)viewWillDisappear:(BOOL)animated
{
    if ([self isBeingDismissed]) {
        [connection removeCallbackBlockFromSender:NSStringFromClass([self class])];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Blurred Snapshot

- (UIImage*)blurredSnapshot
{
    // Create the image context
    UIGraphicsBeginImageContextWithOptions(self.view.bounds.size, YES, self.view.window.screen.scale);
    
    // There he is! The new API method
    [self.view drawViewHierarchyInRect:self.view.frame afterScreenUpdates:NO];
    
    // Get the snapshot
    UIImage *snapshotImage = UIGraphicsGetImageFromCurrentImageContext();
    //[UIImage imageNamed:@"Default@2x.png"];
    
    // Now apply the blur effect using Apple's UIImageEffect category
    UIImage *blurredSnapshotImage = [snapshotImage applyLightEffect];
    
    // Be nice and clean your mess up
    UIGraphicsEndImageContext();
    
    return blurredSnapshotImage;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    MenuViewController* vc = (MenuViewController*)segue.destinationViewController;
    vc.image =[self blurredSnapshot];
}


#pragma mark - Annotations

- (void)addChatroomAnnotation:(ChatroomMessage*)message
{
    // Get local chatrooms from server

    
}


#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    [mapView removeAnnotations:mapView.annotations];
    SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}


#pragma mark - Message I/O

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
        case Chatroom:
            NSLog(@"Chatroom");
            [self addChatroomAnnotation:(ChatroomMessage*)message];
            break;
    }
}

@end
