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
- (void)reconnect;
- (void)sendMessage:(MessageBase*)message;
- (void)streamReset;

@property (nonatomic,strong) NSString* connectMode;

//- (void)connectToImageServer;
//- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url;
//- (int)getImageServerPort;

@end
