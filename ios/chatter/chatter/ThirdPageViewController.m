//
//  ThirdPageViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ThirdPageViewController.h"
#import "ViewController.h"

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
	
    // setup gesture recognizer
    UISwipeGestureRecognizer * recognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(leftSwipeAction)];
    [recognizer setDirection:(UISwipeGestureRecognizerDirectionLeft)];
    [self.view addGestureRecognizer:recognizer];
    
    handleTextField.returnKeyType = UIReturnKeyDone;
    [handleTextField setDelegate:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)leftSwipeAction
{
    if ([handleTextField.text length] > 0) {
        [self performSegueWithIdentifier: @"firsttime3segue" sender: self];
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    ViewController* vc = (ViewController *)segue.destinationViewController;
    vc.userHandle = handleTextField.text;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}



@end
