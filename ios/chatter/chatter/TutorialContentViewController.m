//
//  TutorialContentViewController.m
//  chatter
//
//  Created by sean matthews on 2/5/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "TutorialContentViewController.h"
#import "UserDetails.h"

@interface TutorialContentViewController ()

@end

@implementation TutorialContentViewController

- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self registerForKeyboardNotifications];
    
    viewAdjusted = 0.;
    
    _handleTextField.returnKeyType = UIReturnKeyDone;
    [_handleTextField setDelegate:self];
    
    self.label1.text = self.label1Text;
    self.label2.text = self.label2Text;
    self.label3.text = self.label3Text;
    self.label4.text = self.label4Text;
    self.handleTextField.hidden = !self.showHandleTextField;
    self.goButton.hidden = !self.showGoButton;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - UI Behaviors

- (IBAction)goButtonPressed:(id)sender {
    if ([_handleTextField.text length] > 0) {
        [self performSegueWithIdentifier:@"firstTimeSegue" sender:nil];
    }
    else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:@"Enter a handle" delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
    }
}


#pragma mark - Segue

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"firstTimeSegue"]) {
        UserDetails* ud = [UserDetails sharedInstance];
        ud.handle = _handleTextField.text;
        //MenuViewController* vc = (MenuViewController *)segue.destinationViewController;
        //vc.userHandle = _handleTextField.text;
    }
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
    CGFloat kbDiff = keyboardFrameBeginRect.size.height - _handleTextField.frame.origin.y + _handleTextField.frame.size.height;
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

@end
