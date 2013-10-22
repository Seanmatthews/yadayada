//
//  ViewController.h
//  chattertest
//
//  Created by sean matthews on 10/17/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@interface ViewController : UIViewController <NSStreamDelegate, UITextFieldDelegate, CLLocationManagerDelegate>
{

    NSInputStream *is;
    NSOutputStream *os;
    long long userId;
    long long currentChatroomId;
    long long currentLat, currentLong;
    NSString* userHandle;
    dispatch_queue_t backgroundQueue;
    dispatch_source_t timerSource;
    
    // Location vars
    CLLocationManager *locationManager;
    NSMutableArray *locationMeasurements;
    CLLocation *bestEffortAtLocation;
}

- (void)initConnection;
- (void)updateLocation;
- (void)parseMessage:(uint8_t*)buffer withLength:(int)length;
- (void)writeString:(NSString*)string;
- (void)sendMessage:(NSString*)message toChat:(long long)chatId;
- (void)loginWithUsername:(NSString*)user password:(NSString*)pass;
- (void)registerUsername:(NSString*)user password:(NSString*)pass handle:(NSString*)hand;
- (void)registerHandle:(NSString*)hand;
- (void)leaveChatroomWithId:(long long)chatId;
- (void)joinChatroomWithId:(long long)chatId;
- (void)searchChatrooms;
- (void)createChatroomWithName:(NSString*)name radius:(long long)chatRadius;



- (IBAction)registerButtonPressed:(id)sender;
- (IBAction)loginButtonPressed:(id)sender;
- (IBAction)sendMessageButtonPressed:(id)sender;
- (IBAction)joinChatButtonPressed:(id)sender;
- (IBAction)leaveChatButtonPressed:(id)sender;
- (IBAction)createChatButtonPressed:(id)sender;
- (IBAction)searchChatsButtonPressed:(id)sender;


@property (nonatomic, retain) IBOutlet UITextField* registerUserTextField;
@property (nonatomic, retain) IBOutlet UITextField* registerPassTextField;
@property (nonatomic, retain) IBOutlet UITextField* registerHandTextField;
@property (nonatomic, retain) IBOutlet UITextField* loginUserTextField;
@property (nonatomic, retain) IBOutlet UITextField* loginPassTextField;
@property (nonatomic, retain) IBOutlet UITextField* msgTextField;
@property (nonatomic, retain) IBOutlet UITextField* chatIdTextField;
@property (nonatomic, retain) IBOutlet UITextField* chatNameTextField;
@property (nonatomic, retain) IBOutlet UITextField* chatRadiusTextField;
@property (nonatomic, retain) IBOutlet UITextField* currentChatIdTextField;

@end
