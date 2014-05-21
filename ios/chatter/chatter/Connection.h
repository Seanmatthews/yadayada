//
//  Connection.h
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import Foundation;
#import "MessageUtils.h"


@interface Connection : NSObject <NSStreamDelegate>

+ (id)sharedInstance;
- (id)init;
- (void)connect;
- (void)disconnect;
- (void)reconnect;
- (void)sendMessage:(MessageBase*)message;
- (void)streamReset;
- (void)parsePushNotification:(NSDictionary*)notification;

@property (nonatomic,strong) NSString* connectMode;
@property (nonatomic) NSTimeInterval heartbeatInterval;

//- (void)connectToImageServer;
//- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url;
//- (int)getImageServerPort;

@end
