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
const int WEBVIEW_TAG = 1;
const int ICONVIEW_TAG = 2;
const int HIDDEN_VIEW = 3;

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
        
        iconView = [[UIImageView alloc] init];
        iconView.tag = ICONVIEW_TAG;
        iconView.opaque = NO;
        iconView.userInteractionEnabled = NO;
        
        webview = [[UIWebView alloc] init];
        webview.tag = WEBVIEW_TAG;
        webview.scrollView.scrollEnabled = NO;
        webview.userInteractionEnabled = NO;
        webview.opaque = YES;
        
        hiddenView = [[UIView alloc] init];
        hiddenView.tag = HIDDEN_VIEW;
        hiddenView.opaque = NO;
        hiddenView.backgroundColor = [UIColor clearColor];
        hiddenView.alpha = 0.25;
        hiddenView.frame = self.contentView.bounds; // Should this be set after init?
        hiddenView.userInteractionEnabled = NO;
        
        [self.contentView addSubview:iconView];
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
    frame.size.width = 300;
    frame.size.height = 50;
    [super setFrame:frame];
}

- (void)arrangeElements
{
    CGFloat cellWidth = self.frame.size.width;
    CGFloat cellHeight = self.frame.size.height;
    NSString* html;
    CGFloat heightWithoutBorders = cellHeight-(2*BORDER_WIDTH)-PADDING;
    CGFloat totalPadding = (2*BORDER_WIDTH) + PADDING;
    CGFloat iconViewWidth = heightWithoutBorders;
    CGFloat webViewWidth = cellWidth - iconViewWidth - totalPadding;
    
    // BORDER_WIDTH's worth of natural padding is included after the first element
    if (_selfMessage) {
        webview.frame = CGRectMake(BORDER_WIDTH, BORDER_WIDTH+PADDING, webViewWidth, heightWithoutBorders);
        iconView.frame = CGRectMake(webViewWidth+BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, iconViewWidth, heightWithoutBorders);
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,selfMsgCSS,selfHandleCSS,_message,_userHandle];
    }
    else {
        iconView.frame = CGRectMake(BORDER_WIDTH, BORDER_WIDTH+PADDING, iconViewWidth, heightWithoutBorders);
        webview.frame = CGRectMake(iconViewWidth+BORDER_WIDTH+PADDING, BORDER_WIDTH+PADDING, webViewWidth, heightWithoutBorders);
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,cellMsgCSS,handleCSS,_message,_userHandle];
    }
    iconView.image = _userIcon;
    [webview loadHTMLString:html baseURL:nil];
}


@end
