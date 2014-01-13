//
//  ChatroomMessageCell.m
//  chatter
//
//  Created by sean matthews on 1/9/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "ChatroomMessageCell.h"

@implementation ChatroomMessageCell

const CGFloat BORDER_WIDTH = 2.;
const CGFloat PADDING = 2.;
const CGFloat ICON_SIZE = 50. - (2 * BORDER_WIDTH) - PADDING;
const int CHARS_PER_LINE = 50;
const CGFloat DEFAULT_CELL_HEIGHT = 50.;
const CGFloat HEIGHT_PER_LINE = 15.;


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self.layer setBorderWidth:BORDER_WIDTH];
        [self.layer setBorderColor:[[UIColor groupTableViewBackgroundColor] CGColor]];
        self.accessoryType = UITableViewCellAccessoryNone;
        self.userInteractionEnabled = YES;
        self.selectionStyle = UITableViewCellSelectionStyleNone;
        self.layer.cornerRadius = 5;
        self.layer.masksToBounds = YES;
        
        _userIcon = nil;
        iconView = [[UIImageView alloc] init];
        iconView.opaque = NO;
        iconView.userInteractionEnabled = NO;
        
        webview = [[UIWebView alloc] init];
        webview.scrollView.scrollEnabled = NO;
        webview.userInteractionEnabled = NO;
        webview.opaque = YES;
        
        hiddenView = [[UIView alloc] init];
        hiddenView.opaque = NO;
        hiddenView.backgroundColor = [UIColor clearColor];
        hiddenView.alpha = 0.25;
        hiddenView.frame = self.contentView.bounds; // Should this be set after init?
        hiddenView.userInteractionEnabled = NO;
        
        if (_userIcon) {
            [self.contentView addSubview:iconView];
        }
        [self.contentView addSubview:webview];
        [self.contentView addSubview:hiddenView];
        
        // CSS for table cells
        pageCSS = @"body { margin:0; padding:1; }";
        cellMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:left; vertical-align:text-top; margin:0; padding:0; word-wrap:break-word }";
        handleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#D0D0D0 }";
        selfMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:right; vertical-align:text-top; margin:0; padding:0; word-wrap:break-word }";
        selfHandleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#D0D0D0; text-align:right }";
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)setFrame:(CGRect)frame {
    [super setFrame:frame];
}

- (void)arrangeElements
{
    [self setFrame:CGRectMake(self.frame.origin.x, self.frame.origin.y, 300, [ChatroomMessageCell heightForText:_message])];
    
    CGFloat cellWidth = self.frame.size.width;
    CGFloat cellHeight = self.frame.size.height;
    NSString* html;
    CGFloat heightWithoutBorders = cellHeight-(2*BORDER_WIDTH)-PADDING;
    CGFloat totalPadding = (2*BORDER_WIDTH) + PADDING;
    CGFloat webViewWidth = cellWidth - ICON_SIZE - totalPadding;
    
    // BORDER_WIDTH's worth of natural padding is included after the first element
    if (_selfMessage) {
        if (_userIcon) {
            webview.frame = CGRectMake(BORDER_WIDTH, BORDER_WIDTH+PADDING, webViewWidth, heightWithoutBorders);
            iconView.frame = CGRectMake(webViewWidth+BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, ICON_SIZE, ICON_SIZE);
        }
        else {
            webview.frame = CGRectMake(BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, webViewWidth+ICON_SIZE, heightWithoutBorders);
        }
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,selfMsgCSS,selfHandleCSS,_message,_userHandle];
    }
    else {
        if (_userIcon) {
            iconView.frame = CGRectMake(BORDER_WIDTH, BORDER_WIDTH+PADDING, ICON_SIZE, ICON_SIZE);
            webview.frame = CGRectMake(ICON_SIZE+BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, webViewWidth, heightWithoutBorders);
        }
        else {
            webview.frame = CGRectMake(BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, webViewWidth+ICON_SIZE, heightWithoutBorders);
        }
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,cellMsgCSS,handleCSS,_message,_userHandle];
    }
    iconView.image = _userIcon;
    [webview loadHTMLString:html baseURL:nil];
}

+ (CGFloat)heightForText:(NSString*)text
{
    return MAX(DEFAULT_CELL_HEIGHT, HEIGHT_PER_LINE * ( ceilf((CGFloat)text.length / (CGFloat)CHARS_PER_LINE) + 1.)); // 1 extra for the handle line
}


@end
