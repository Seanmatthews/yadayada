//
//  TutorialContentViewController.h
//  chatter
//
//  Created by sean matthews on 2/5/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TutorialContentViewController : UIViewController <UITextFieldDelegate>
{
    CGFloat viewAdjusted;
}

@property (weak, nonatomic) IBOutlet UILabel* label1;
@property (weak, nonatomic) IBOutlet UILabel* label2;
@property (weak, nonatomic) IBOutlet UILabel* label3;
@property (weak, nonatomic) IBOutlet UILabel* label4;
@property (weak, nonatomic) IBOutlet UITextField* handleTextField;
@property (weak, nonatomic) IBOutlet UIButton* goButton;

@property NSUInteger pageIndex;
@property NSString* label1Text;
@property NSString* label2Text;
@property NSString* label3Text;
@property NSString* label4Text;
@property BOOL showHandleTextField;
@property BOOL showGoButton;

- (IBAction)goButtonPressed:(id)sender;

@end
