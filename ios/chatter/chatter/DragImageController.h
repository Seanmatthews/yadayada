//
//  DragImageController.h
//  chatter
//
//  Created by sean matthews on 11/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DragImageController : UIViewController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>
{
    CGPoint lastTrans;
}

@property (nonatomic, retain) IBOutlet UIImageView* imageView;
@property (nonatomic) UIImagePickerController *imagePickerController;

@end
