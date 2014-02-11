//
//  CreateChatViewController.h
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Connection.h"
#import "Location.h"
#import "UserDetails.h"
#import "ChatroomManagement.h"

@interface CreateChatViewController : UIViewController <UITextFieldDelegate>
{
    Connection* connection;
    Location* location;
    UserDetails* ud;
    long long chatroomId;
    BOOL viewIsVisible;
    ChatroomManagement* chatManager;
}

@property (nonatomic, retain) IBOutlet UITextField* chatroomNameTextField;
@property (nonatomic, retain) IBOutlet UIButton* chatroomIconButton;
@property (nonatomic, retain) IBOutlet UILabel* chatroomRadiusLabel;
@property (nonatomic, retain) IBOutlet UIImageView* iconView;
@property (nonatomic, retain) IBOutlet UISlider* radiusSlider;
@property (nonatomic, retain) IBOutlet UISegmentedControl* globalChatSelect;

- (void)messageCallback:(MessageBase*)message;
- (void)joinCreatedChatroom;
- (void)leaveCurrentChatroom;
- (IBAction)sliderChanged:(id)sender;
- (IBAction)createChatroom:(id)sender;
- (IBAction)globalChatSelection:(id)sender;


@end
