//
//  UserDetails.h
//  chatter
//
//  Created by sean matthews on 10/27/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UserDetails : NSObject
{
    
}

@property BOOL finishedTutorial;
@property (readwrite, nonatomic) NSString* handle;
@property NSString* UUID;
@property long long userId;
@property NSString* userIconName;
@property BOOL registeredHandle;
@property BOOL receiveChatroomNotifications;
@property BOOL receiveMessageNotifications;

// TODO: The type of this property will change ot accommodate
// being joined to several chats at once.
@property long long chatroomId;

+ (id)sharedInstance;
+ (void)save;
- (id)init;
- (id) initWithHandle:(NSString*)handle;
- (void)setHandle:(NSString *)handle;

@end
