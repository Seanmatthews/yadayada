//
//  ChatroomListCell.h
//  chatter
//
//  Created by Jim Greco on 11/12/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatroomListCell : UITableViewCell

@property (nonatomic, retain) IBOutlet UILabel* chatroomName;
@property (nonatomic, retain) IBOutlet UILabel* milesFromOrigin;
@property (nonatomic, retain) IBOutlet UILabel* numberOfUsers;
@property (nonatomic, retain) IBOutlet UILabel* percentActive;
@property (nonatomic, retain) IBOutlet UIImageView* chatroomImage;


@end
