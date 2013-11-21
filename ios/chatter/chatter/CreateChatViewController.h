//
//  CreateChatViewController.h
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CreateChatViewController : UIViewController
{
    
}

@property (nonatomic, retain) IBOutlet UITextField* chatroomNameTextField;
@property (nonatomic, retain) IBOutlet UIButton* chatroomIconButton;
@property (nonatomic, retain) IBOutlet UILabel* chatroomRadiusLabel;

- (IBAction)sliderChanged:(id)sender;

@end
