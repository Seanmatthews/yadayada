//
//  TutorialStartController.h
//  chatter
//
//  Created by sean matthews on 2/6/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TutorialStartController : UIViewController <UIPageViewControllerDataSource>


@property (strong, nonatomic) UIPageViewController *pageViewController;
@property (strong, nonatomic) NSArray *label1;
@property (strong, nonatomic) NSArray *label2;
@property (strong, nonatomic) NSArray *label3;
@property (strong, nonatomic) NSArray *label4;
@property (strong, nonatomic) NSArray *showTextField;
@property (strong, nonatomic) NSArray *showGoButton;

@end
