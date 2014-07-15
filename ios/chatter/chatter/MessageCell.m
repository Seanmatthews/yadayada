//
//  MessageCell.m
//  chatter
//
//  Created by sean matthews on 5/22/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "MessageCell.h"

const int MESSAGE_CHAR_LIMIT = 200; // Max characters allowed
const CGFloat MAX_CELL_WIDTH = 250; // Width of the message container
const CGFloat BORDER_WIDTH = 4.; // The border around the UITableViewCell that offsets cells
const CGFloat MSG_PADDING = 7.; // Padding within NSAttributedString object that contains the message
const CGFloat MSG_OFFSET = 10.; // Offset from the side of the screen

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
    p1.headIndent = MSG_PADDING;
    p1.firstLineHeadIndent = MSG_PADDING;
    NSMutableParagraphStyle *p2 = [[NSMutableParagraphStyle alloc] init];
    
    
    
    
    
    // Set label size and position within the cell
    CGSize labelSize = [MessageCell sizeForText:_message username:_userHandle];
    CGRect labelFrame = CGRectMake(MSG_OFFSET, 0, labelSize.width, labelSize.height);
    
    // if this is a self message, right justify the label
    if (_selfMessage) {
        labelFrame.origin.x = self.layer.bounds.size.width - labelSize.width - MSG_OFFSET;
        p2.alignment = NSTextAlignmentRight;
        p2.tailIndent = -MSG_PADDING;
    }
    else {
        p2.alignment = NSTextAlignmentLeft;
        p2.headIndent = MSG_PADDING;
        p2.firstLineHeadIndent = MSG_PADDING;
    }
    
    [messageText addAttribute:NSParagraphStyleAttributeName value:p1 range:NSMakeRange(0, _message.length+1)];
    [messageText addAttribute:NSParagraphStyleAttributeName value:p2 range:NSMakeRange(_message.length+1, _userHandle.length)];
    
    UILabel* labelView = [[UILabel alloc] initWithFrame:labelFrame];
    labelView.attributedText = messageText;
    [labelView setBackgroundColor:[UIColor whiteColor]];
    labelView.layer.cornerRadius = 15;
    labelView.layer.masksToBounds = YES;
    labelView.lineBreakMode = NSLineBreakByWordWrapping;
    labelView.numberOfLines = 0;
    
    // Hidden view is to catch all user interactions?
    UIView* hiddenView = [[UIView alloc] init];
    hiddenView.opaque = NO;
    hiddenView.backgroundColor = [UIColor clearColor];
    hiddenView.alpha = 0.25;
    hiddenView.frame = labelView.frame;
    hiddenView.bounds = labelView.bounds;
    hiddenView.userInteractionEnabled = NO;
    hiddenView.layer.cornerRadius = 15;
    hiddenView.layer.masksToBounds = YES;
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
                     value:[UIColor grayColor]
                     range:NSMakeRange(0, [attrUser length])];
    
    [attrText appendAttributedString:attrUser];
    
    return attrText;
}

+ (CGSize)sizeForText:(NSString*)text username:(NSString*)username
{
    NSMutableAttributedString* attrText = [MessageCell attributedStringFromMessage:text username:username];
    UILabel *gettingSizeLabel = [[UILabel alloc] init];
    gettingSizeLabel.attributedText = attrText;
    gettingSizeLabel.numberOfLines = 0;
    gettingSizeLabel.lineBreakMode = NSLineBreakByWordWrapping;
    CGSize maximumLabelSize = CGSizeMake(MAX_CELL_WIDTH, 9999);
    
    CGSize expectedSize = [gettingSizeLabel sizeThatFits:maximumLabelSize];
    expectedSize.height = ceilf(expectedSize.height) + 2 * MSG_PADDING;
    expectedSize.width = ceilf(expectedSize.width) + 2 * MSG_PADDING;
    return expectedSize;
}

@end
