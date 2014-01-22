//
//  ThirdPageViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ThirdPageViewController.h"
#import "MenuViewController.h"


@interface ThirdPageViewController ()

@end

@implementation ThirdPageViewController

@synthesize handleTextField;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    viewAdjusted = 0.;
	
    // setup gesture recognizer
    UISwipeGestureRecognizer * recognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(leftSwipeAction)];
    [recognizer setDirection:(UISwipeGestureRecognizerDirectionLeft)];
    [self.view addGestureRecognizer:recognizer];
    
    handleTextField.returnKeyType = UIReturnKeyDone;
    [handleTextField setDelegate:self];
    
    [self registerForKeyboardNotifications];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Keyboard Interaction

- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillBeHidden:) name:UIKeyboardWillHideNotification object:nil];
}

// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    const CGFloat PADDING = 20;
    
    // Get the keyboard frame
    NSDictionary* keyboardInfo = [aNotification userInfo];
    NSValue* keyboardFrameBegin = [keyboardInfo valueForKey:UIKeyboardFrameBeginUserInfoKey];
    CGRect keyboardFrameBeginRect = [keyboardFrameBegin CGRectValue];
    
    // Move the view so that the text field is just above the keyboard
    CGFloat kbDiff = keyboardFrameBeginRect.size.height - handleTextField.frame.origin.y + handleTextField.frame.size.height;
    if (kbDiff < PADDING) {
        CGRect viewFrame = self.view.frame;
        viewFrame.origin.y += kbDiff - PADDING;
        self.view.frame = viewFrame;
        viewAdjusted = kbDiff - PADDING;
    }
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    if (viewAdjusted < 0.) {
        CGRect viewFrame = self.view.frame;
        viewFrame.origin.y -= viewAdjusted;
        self.view.frame = viewFrame;
        viewAdjusted = 0.;
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - Navigation

- (void)leftSwipeAction
{
    if ([handleTextField.text length] > 0) {
        [self performSegueWithIdentifier: @"firstTimeSegue" sender:self];
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"firstTimeSegue"]) {
        MenuViewController* vc = (MenuViewController *)segue.destinationViewController;
        vc.userHandle = handleTextField.text;
    }
}





@end
