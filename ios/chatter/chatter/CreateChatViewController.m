//
//  CreateChatViewController.m
//  chatter
//
//  Created by sean matthews on 11/20/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "CreateChatViewController.h"
#import "Messages.h"

@interface CreateChatViewController ()

@end

@implementation CreateChatViewController


- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        connection = [Connection sharedInstance];
        location = [Location sharedInstance];
        ud = [UserDetails sharedInstance];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)sliderChanged:(id)sender
{
    UISlider* slider = (UISlider*)sender;
    [_chatroomRadiusLabel setText:[NSString stringWithFormat:@"%f",slider.value]];
}

- (IBAction)createChatroom:(id)sender
{
    CreateChatroomMessage* msg = [[CreateChatroomMessage alloc] init];
    msg.chatroomName = _chatroomNameTextField.text;
    msg.radius = [_chatroomRadiusLabel.text floatValue];
    msg.ownerId = ud.userId;
    msg.latitude = [location currentLat];
    msg.longitude = [location currentLong];
    [connection sendMessage:msg];
}

- (IBAction)unwindToPreviousView:(id)sender
{
    NSLog(@"%@",_unwindSegueName);
    [self performSegueWithIdentifier:_unwindSegueName sender:self];
}

@end
