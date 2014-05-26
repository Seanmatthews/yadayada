//
//  MessageCell.m
//  chatter
//
//  Created by sean matthews on 5/22/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "MessageCell.h"

const int MESSAGE_CHAR_LIMIT = 200;
const CGFloat MAX_CELL_WIDTH = 300;
const CGFloat BORDER_WIDTH = 2.;
const CGFloat PADDING = 2.;

@interface MessageCell ()

+ (NSMutableAttributedString*)attributedStringFromMessage:(NSString*)message username:(NSString*)username;

@end

@implementation MessageCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self.layer setBorderWidth:BORDER_WIDTH];
        [self.layer setBorderColor:[[UIColor groupTableViewBackgroundColor] CGColor]];
        [self setBackgroundColor:[UIColor clearColor]];
        self.accessoryType = UITableViewCellAccessoryNone;
        self.userInteractionEnabled = YES;
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.layer.cornerRadius = 10;
        self.layer.masksToBounds = YES;
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setFrame:(CGRect)frame {
    [super setFrame:frame];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)arrangeElements
{
    // Apply font properties to the message and username
    NSMutableAttributedString* messageText = [MessageCell attributedStringFromMessage:_message username:_userHandle];
    
    // Must add paragraph styles after view sizing (based on message) has been done--
    // otherwise, the view will not be sized properly.
    NSMutableParagraphStyle *p1 = [[NSMutableParagraphStyle alloc] init];
    p1.alignment = NSTextAlignmentLeft;
    NSMutableParagraphStyle *p2 = [[NSMutableParagraphStyle alloc] init];
    p2.alignment = NSTextAlignmentRight;
    
    [messageText addAttribute:NSParagraphStyleAttributeName value:p1 range:NSMakeRange(0, _message.length+1)];
    [messageText addAttribute:NSParagraphStyleAttributeName value:p2 range:NSMakeRange(_message.length+1, _userHandle.length)];
    
    // Set label size and position within the cell
    CGSize labelSize = [MessageCell sizeForText:_message username:_userHandle];
    CGRect labelFrame = CGRectMake(0, 0, labelSize.width, labelSize.height);
    
    // if this is a self message, right justify the label
    if (_selfMessage) {
        labelFrame.origin.x = MAX_CELL_WIDTH - labelSize.width;
    }
    
    UILabel* labelView = [[UILabel alloc] initWithFrame:labelFrame];
    labelView.attributedText = messageText;
    [labelView setBackgroundColor:[UIColor whiteColor]];
    labelView.layer.cornerRadius = 10;
    labelView.layer.masksToBounds = YES;
    labelView.lineBreakMode = NSLineBreakByWordWrapping;
    labelView.numberOfLines = 0;
    
    // Hidden view is to catch all user interactions?
    UIView* hiddenView = [[UIView alloc] init];
    hiddenView.opaque = NO;
    hiddenView.backgroundColor = [UIColor clearColor];
    hiddenView.alpha = 0.25;
    hiddenView.frame = self.contentView.bounds; // Should this be set after init?
    hiddenView.userInteractionEnabled = NO;
    hiddenView.tag = 3;
    
    // Add the views, bottom first
    [self.contentView addSubview:labelView];
    [self.contentView addSubview:hiddenView]; // add this last (on top)
}

// The constants that define text style for all parts of the messages are contained within this method
+ (NSMutableAttributedString*)attributedStringFromMessage:(NSString*)message username:(NSString*)username
{
    // Add the text and username to NSAttributed string with relevant properties
    NSMutableAttributedString* attrText = [[NSMutableAttributedString alloc]
                                           initWithString:[NSString stringWithFormat:@"%@",message]];
    [attrText addAttribute:NSFontAttributeName
                     value:[UIFont systemFontOfSize:14.]
                     range:NSMakeRange(0, [attrText length])];
    
    NSMutableAttributedString* attrUser = [[NSMutableAttributedString alloc]
                                           initWithString:[NSString stringWithFormat:@"\n%@",username]];
    [attrUser addAttribute:NSFontAttributeName
                     value:[UIFont boldSystemFontOfSize:13.]
                     range:NSMakeRange(0, [attrUser length])];
    [attrUser addAttribute:NSForegroundColorAttributeName
                     value:[UIColor greenColor]
                     range:NSMakeRange(0, [attrUser length])];
    
    [attrText appendAttributedString:attrUser];
    
    return attrText;
}

+ (CGSize)sizeForText:(NSString*)text username:(NSString*)username
{
    NSMutableAttributedString* attrText = [MessageCell attributedStringFromMessage:text username:username];
    
    CGFloat totalPadding = (2*BORDER_WIDTH) + PADDING;
    UILabel *gettingSizeLabel = [[UILabel alloc] init];
    gettingSizeLabel.attributedText = attrText;
    gettingSizeLabel.numberOfLines = 0;
    gettingSizeLabel.lineBreakMode = NSLineBreakByWordWrapping;
    CGSize maximumLabelSize = CGSizeMake(300, 9999);
    
    CGSize expectedSize = [gettingSizeLabel sizeThatFits:maximumLabelSize];
    expectedSize.height = ceilf(expectedSize.height) + totalPadding;
    expectedSize.width = ceilf(expectedSize.width) + totalPadding;
    NSLog(@"width: %f height: %f", expectedSize.width, expectedSize.height);
    return expectedSize;
}

@end
