//
//  SnarkyErrorMessages.h
//  chatter
//
//  Created by sean matthews on 6/24/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SnarkyErrorMessages : NSObject

+ (id)sharedInstance;
- (NSString*)messageForConnectionError;
- (NSString*)messageForInviteSelf;

@end
