//
//  MessageCell.h
//  chatter
//
//  Created by sean matthews on 5/22/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Messages.h"

extern const int MESSAGE_CHAR_LIMIT;
extern const CGFloat BORDER_WIDTH;
extern const CGFloat PADDING;

@interface MessageCell : UITableViewCell

@property NSString* userHandle;
@property NSString* message;
@property BOOL selfMessage;

// Currently using this as a reference to the message in a chatroom's chat queue
@property (nonatomic,strong) MessageMessage* messageObj;

- (void)arrangeElements;
+ (CGSize)sizeForText:(NSString*)text username:(NSString*)username;


@end
