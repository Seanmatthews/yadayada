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
        cellMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:left; vertical-align:text-top; margin:0; padding:0 }";
        handleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#D0D0D0 }";
        selfMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:right; vertical-align:text-top; margin:0; padding:0 }";
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
    
    if (_selfMessage) {
        iconView.frame = CGRectMake(cellWidth-cellHeight, 0, cellHeight, cellHeight);
        webview.frame = CGRectMake(0, 0, cellWidth-cellHeight, cellHeight);
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,selfMsgCSS,selfHandleCSS,_message,_userHandle];
    }
    else {
        iconView.frame = CGRectMake(0, 0, cellHeight, cellHeight);
        webview.frame = CGRectMake(44, 0, cellWidth-cellHeight, cellHeight);
        html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",
                pageCSS,cellMsgCSS,handleCSS,_message,_userHandle];
    }
    iconView.image = _userIcon;
    [webview loadHTMLString:html baseURL:nil];
}


@end
