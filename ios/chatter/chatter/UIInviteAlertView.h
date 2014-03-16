//
//  UIInviteAlertView.h
//  chatter
//
//  Created by sean matthews on 2/24/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Chatroom.h"

@interface UIInviteAlertView : UIAlertView

@property (nonatomic, strong) Chatroom* chatroom;

@end
