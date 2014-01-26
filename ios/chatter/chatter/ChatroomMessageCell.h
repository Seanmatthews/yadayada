//
//  ChatroomMessageCell.h
//  chatter
//
//  Created by sean matthews on 1/9/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserDetails.h"

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

- (void)arrangeElements;
+ (CGFloat)heightForText:(NSString*)text;
+ (int)getMessageCharLimit;

@end
