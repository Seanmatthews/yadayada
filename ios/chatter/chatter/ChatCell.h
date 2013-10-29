//
//  ChatCell.h
//  chatter
//
//  Created by sean matthews on 10/29/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatCell : UITableViewCell
{
    UIImageView* userIcon;
    UILabel* userHandle;
    UIWebView* msgText;
}


@end
