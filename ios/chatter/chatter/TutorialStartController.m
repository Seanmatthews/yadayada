//
//  TutorialStartController.m
//  chatter
//
//  Created by sean matthews on 2/6/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "TutorialStartController.h"
#import "TutorialContentViewController.h"

@interface TutorialStartController ()

@end

@implementation TutorialStartController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Create the data model
    _label1 = @[@"Find nearby chatrooms", @"Double-tap relevant messages", @"Create a handle"];
    _label2 = @[@"or create a chatroom", @"and swipe the spam.", @"and begin chatting"];
    _label3 = @[@"at your location.", @"Popular messages will", @"in global chat!"];
    _label4 = @[@"", @"gain wider exposure.", @""];
    _showTextField = [NSArray arrayWithObjects:[NSNumber numberWithBool:NO],
                      [NSNumber numberWithBool:NO], [NSNumber numberWithBool:YES], nil];
    _showGoButton = [NSArray arrayWithObjects:[NSNumber numberWithBool:NO],
                     [NSNumber numberWithBool:NO], [NSNumber numberWithBool:YES], nil];
    
    // Create page view controller
    self.pageViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"TutorialPageViewController"];
    self.pageViewController.dataSource = self;
    
    
    TutorialContentViewController *startingViewController = [self viewControllerAtIndex:0];
    NSArray *viewControllers = @[startingViewController];
    [self.pageViewController setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    
    // Change the size of page view controller
    self.pageViewController.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - 30);
    
    [self addChildViewController:_pageViewController];
    [self.view addSubview:_pageViewController.view];
    [self.pageViewController didMoveToParentViewController:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Page View Controller Data Source

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSUInteger index = ((TutorialContentViewController*) viewController).pageIndex;
    
    if ((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    
    index--;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    NSUInteger index = ((TutorialContentViewController*) viewController).pageIndex;
    
    if (index == NSNotFound) {
        return nil;
    }
    
    index++;
    if (index == [self.label1 count]) {
        return nil;
    }
    return [self viewControllerAtIndex:index];
}

- (TutorialContentViewController *)viewControllerAtIndex:(NSUInteger)index
{
    if (([self.label1 count] == 0) || (index >= [self.label1 count])) {
        return nil;
    }
    
    // Create a new view controller and pass suitable data.
    TutorialContentViewController *pageContentViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"TutorialContentVC"];
    pageContentViewController.label1Text = _label1[index];
    pageContentViewController.label2Text = _label2[index];
    pageContentViewController.label3Text = _label3[index];
    pageContentViewController.label4Text = _label4[index];
    pageContentViewController.showHandleTextField = [_showTextField[index] boolValue];
    pageContentViewController.showGoButton = [_showGoButton[index] boolValue];
    pageContentViewController.pageIndex = index;
    
    return pageContentViewController;
}

- (NSInteger)presentationCountForPageViewController:(UIPageViewController *)pageViewController
{
    return [self.label1 count];
}

- (NSInteger)presentationIndexForPageViewController:(UIPageViewController *)pageViewController
{
    return 0;
}





@end
