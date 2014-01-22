//
//  ThirdPageViewController.h
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ThirdPageViewController : UIViewController <UITextFieldDelegate>
{
    CGFloat viewAdjusted;
}

- (void)registerForKeyboardNotifications;
- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;

@property (nonatomic, retain) IBOutlet UITextField* handleTextField;
//@property (nonatomic, retain) IBOutlet UIScrollView* scrollView;
@property (nonatomic, retain) IBOutlet UILabel* bottomLabel;

@end
