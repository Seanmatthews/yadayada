//
//  DragImageController.m
//  chatter
//
//  Created by sean matthews on 11/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "DragImageController.h"

@interface DragImageController ()

@end

@implementation DragImageController

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
	UITapGestureRecognizer* tapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapped:)];
    tapped.numberOfTapsRequired = 1;
    [self.view addGestureRecognizer:tapped];
    UIPanGestureRecognizer* panned = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panned:)];
    [self.view addGestureRecognizer:panned];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)tapped:(UITapGestureRecognizer*)sender
{
    NSLog(@"tapped");
    UIImagePickerController *ipc = [[UIImagePickerController alloc] init];
    ipc.modalPresentationStyle = UIModalPresentationCurrentContext;
    ipc.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    ipc.delegate = self;
    _imagePickerController = ipc;
    [self presentViewController:_imagePickerController animated:YES completion:nil];
}

- (void)panned:(UIPanGestureRecognizer*)sender
{
    NSLog(@"panned");
    CGPoint location = [sender locationInView:self.view];
    if (location.x > 0 && location.y > 0 &&
        location.x < self.view.bounds.origin.x+self.view.bounds.size.width &&
        location.y < self.view.bounds.origin.y+self.view.bounds.size.height) {
        _imageView.center = location;
    }
}


#pragma mark - UIImagePickerControllerDelegate

// This method is called when an image has been chosen from the library or taken from the camera.
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    _imageView.image = [info valueForKey:UIImagePickerControllerOriginalImage];
    [self dismissViewControllerAnimated:YES completion:NULL];
    _imagePickerController = nil;
}


- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    [self dismissViewControllerAnimated:YES completion:NULL];
}

@end