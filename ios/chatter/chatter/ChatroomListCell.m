//
//  ChatroomListCell.m
//  chatter
//
//  Created by Jim Greco on 11/12/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ChatroomListCell.h"

@implementation ChatroomListCell

@synthesize chatroomName;


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
