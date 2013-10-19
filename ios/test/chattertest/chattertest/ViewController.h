//
//  ViewController.h
//  chattertest
//
//  Created by sean matthews on 10/17/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

@interface ViewController : UIViewController <NSStreamDelegate>
{

    NSInputStream *is;
    NSOutputStream *os;
    long long userId;
    
}

- (void)initConnection;

- (IBAction)registerButtonPressed:(id)sender;
- (IBAction)loginButtonPressed:(id)sender;
- (IBAction)sendMessageButtonPressed:(id)sender;

@property (nonatomic, retain) IBOutlet UITextField* registerUserTextField;
@property (nonatomic, retain) IBOutlet UITextField* registerPassTextField;
@property (nonatomic, retain) IBOutlet UITextField* loginUserTextField;
@property (nonatomic, retain) IBOutlet UITextField* loginPassTextField;
@property (nonatomic, retain) IBOutlet UITextField* msgTextField;

@end
