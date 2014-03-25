//
//  UserDetails.h
//  chatter
//
//  Created by sean matthews on 10/27/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

@import Foundation;

@interface UserDetails : NSObject

@property BOOL finishedTutorial;
@property (readwrite, nonatomic) NSString* handle;
@property NSString* UUID;
@property long long userId;
@property NSString* userIconName;
@property BOOL registeredHandle;
@property BOOL receiveChatroomNotifications;
@property BOOL receiveMessageNotifications;
@property BOOL receiveInviteNotifications;
@property UIImage* userIcon;
@property (nonatomic,strong) NSArray* joinedChatroomIds;


// This is not a user detail, but rather an app global
@property NSString* iconUploadURL;
@property NSString* iconDownloadURL;

+ (id)sharedInstance;
+ (void)save;
- (id)init;
- (id)initWithHandle:(NSString*)handle;
- (void)setHandle:(NSString *)handle;


@end
