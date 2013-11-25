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

@interface CreateChatViewController : UIViewController
{
    Connection* connection;
    Location* location;
    UserDetails* ud;
}

@property (nonatomic, retain) IBOutlet UITextField* chatroomNameTextField;
@property (nonatomic, retain) IBOutlet UIButton* chatroomIconButton;
@property (nonatomic, retain) IBOutlet UILabel* chatroomRadiusLabel;
@property NSString* unwindSegueName;
@property (nonatomic, retain) IBOutlet UIImageView* iconView;


- (IBAction)sliderChanged:(id)sender;
- (IBAction)createChatroom:(id)sender;
- (IBAction)unwindToPreviousView:(id)sender;


@end
