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
#import "ViewController.h"
#import "ChatPointAnnotation.h"
#import "SmartButton.h"


@interface MapViewController ()

@end

@implementation MapViewController

- (void)initCode
{
    location = [Location sharedInstance];
    ud = [UserDetails sharedInstance];
    
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
    viewIsVisible = YES;
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
    viewIsVisible = NO;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)locateButtonPressed:(id)sender
{
    [_mapView setCenterCoordinate:[location currentLocation] animated:YES];
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


#pragma mark - Annotations

- (void)addChatroomAnnotation:(ChatroomMessage*)message
{
    ChatPointAnnotation* mpa = [[ChatPointAnnotation alloc] init];
    CLLocationCoordinate2D coord;
    coord = [Location fromLongLongLatitude:message.latitude Longitude:message.longitude];
    
    // Need this check because in case of bad lat/long data.
    // iOS throws a nondescript deallocation error if you try to draw out of bounds lat/long annotations.
    if (coord.latitude >= -90. && coord.latitude <=90. && coord.longitude >= -180. && coord.longitude <= 180.) {
        NSNumberFormatter* format = [[NSNumberFormatter alloc] init];
        [format setNumberStyle:NSNumberFormatterDecimalStyle];
        [format setMaximumFractionDigits:2];
        
        mpa.chatroomId = message.chatroomId;
        mpa.coordinate = coord;
        mpa.title = message.chatroomName;
        CGFloat milesToChat = [location milesToCurrentLocationFrom:mpa.coordinate];
        mpa.subtitle = [NSString stringWithFormat:@"%@miles  %dusers  %d%%",[format stringFromNumber:[NSNumber numberWithFloat:milesToChat]],message.userCount,message.chatActivity];
        [_mapView addAnnotation:mpa];
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


#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    NSLog(@"segue: %@",segue.identifier);
    if ([segue.identifier isEqualToString:@"ChatAnnotation"]) {
        JoinedChatroomMessage* jcm = (JoinedChatroomMessage*)sender;
        ViewController* vc = (ViewController*)segue.destinationViewController;
        vc.chatId = jcm.chatroomId;
        vc.chatTitle = jcm.chatroomName;
        //[_mapView deselectAnnotation:mpa animated:NO];
        //[self deselectAllAnnotations];
    }
}

- (void)joinChatroom:(id)sender
{
    NSLog(@"join chatroom");
//    MKPinAnnotationView* mkp = (MKPinAnnotationView*)(((SmartButton*)sender).parent);
//    ChatPointAnnotation* cpa = (ChatPointAnnotation*)mkp.annotation;
    ChatPointAnnotation* cpa = (ChatPointAnnotation*)(((SmartButton*)sender).parent);
    JoinChatroomMessage* msg = [[JoinChatroomMessage alloc] init];
    
    msg.chatroomId = cpa.chatroomId;
    msg.userId = ud.userId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    NSLog(@"chatid: %lld",msg.chatroomId);
    NSLog(@"userId: %lld",msg.userId);
    NSLog(@"%f, %f",[Location fromLongLong:msg.latitude],[Location fromLongLong:msg.longitude]);
    [connection sendMessage:msg];
    [_mapView deselectAnnotation:cpa animated:NO];
    NSLog(@"out of join chatroom");
}


#pragma mark - MKMapViewDelegate methods

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    [mapView removeAnnotations:mapView.annotations];
    SearchChatroomsMessage* msg = [[SearchChatroomsMessage alloc] init];
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    msg.onlyJoinable = YES;
    msg.metersFromCoords = 1609.34 * 5.; // TODO: change to screen region bounds
    [connection sendMessage:msg];
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
        [disclosureButton addTarget:self action:@selector(joinChatroom:) forControlEvents:UIControlEventTouchUpInside];
        annotationView.rightCalloutAccessoryView = disclosureButton;
        return annotationView;
    }
    return annotationView;
}


#pragma mark - Message I/O

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
        case Chatroom:
            NSLog(@"Chatroom");
            [self addChatroomAnnotation:(ChatroomMessage*)message];
            break;
            
        case JoinedChatroom:
            NSLog(@"joined chatroom");
            [self performSegueWithIdentifier:@"ChatAnnotation" sender:message];
            break;
            
        case JoinChatroomReject:
            NSLog(@"%@",((JoinChatroomRejectMessage*)message).reason);
            break;
    }
}

@end
