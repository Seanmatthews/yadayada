//
//  ChatroomMessageCell.h
//  chatter
//
//  Created by sean matthews on 1/9/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

@import UIKit;
#import "UserDetails.h"
#import "Messages.h"

@interface ChatroomMessageCell : UITableViewCell
{
    NSString* cellMsgCSS;
    NSString* handleCSS;
    NSString* pageCSS;
    NSString* selfMsgCSS;
    NSString* selfHandleCSS;
    
    UIWebView* webview;
    UIImageView* iconView;
    UIView* hiddenView;
}

@property NSString* userHandle;
@property UIImage* userIcon;
@property NSString* message;
@property BOOL selfMessage;

// Currently using this as a reference to the message in a chatroom's chat queue
@property (nonatomic,strong) MessageMessage* messageObj;

- (void)arrangeElements;
+ (CGFloat)heightForText:(NSString*)text;
+ (int)getMessageCharLimit;

@end
