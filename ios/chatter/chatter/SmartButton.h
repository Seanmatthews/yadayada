//
//  SmartButton.h
//  chatter
//
//  Created by sean matthews on 1/7/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SmartButton : UIButton

// No need to keep a reference to the parent when we just want
// the values at the time of the copy.
@property id parent;

@end
