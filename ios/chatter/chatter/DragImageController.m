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
    
    lastTrans = CGPointMake(0., 0.);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Gesture Recognizers

- (void)tapped:(UITapGestureRecognizer*)sender
{
//    NSLog(@"tapped");
    //UIAlertView *alert = [[UIAlertView alloc] initWithTitle: @"Woops!" message:@"No icons available" delegate: nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
    //[alert show];
    
    UIImagePickerController *ipc = [[UIImagePickerController alloc] init];
    ipc.modalPresentationStyle = UIModalPresentationCurrentContext;
    ipc.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    ipc.delegate = self;
    _imagePickerController = ipc;
    [self presentViewController:_imagePickerController animated:YES completion:nil];
}

- (void)panned:(UIPanGestureRecognizer*)sender
{
    CGPoint trans = [sender translationInView:_imageView];
    CGPoint newTrans = CGPointMake(trans.x-lastTrans.x, trans.y-lastTrans.y);
    CGPoint location = [sender locationInView:self.view];
    if (location.x > 0 && location.y > 0 &&
        location.x < self.view.bounds.origin.x+self.view.bounds.size.width &&
        location.y < self.view.bounds.origin.y+self.view.bounds.size.height) {
        CGPoint newLoc;
        newLoc.x = _imageView.center.x + newTrans.x;
        newLoc.y = _imageView.center.y + newTrans.y;
        _imageView.center = newLoc;
        lastTrans = trans;
    }
    
    CGRect coords = [self.view convertRect:self.view.frame toView:_imageView];
    NSLog(@"coords %f, %f",coords.origin.x,coords.origin.y);
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    lastTrans.x = 0.;
    lastTrans.y = 0.;
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


#pragma mark - Image upload

- (UIImage*)cropImage
{
    UIImage* img = _imageView.image;
    CGRect cropRect = [self.view convertRect:self.view.frame toView:_imageView];
    CGImageRef imgRef = CGImageCreateWithImageInRect((__bridge CGImageRef)(img), cropRect);
    UIImage* cropped = [UIImage imageWithCGImage:imgRef];
    CGImageRelease(imgRef);
    return cropped;
}

- (BOOL)uploadImage
{
    NSData *imageData = UIImageJPEGRepresentation([self cropImage], 90);
    // setting up the URL to post to
    NSString *urlString = @"http://iphone.zcentric.com/test-upload.php";
    
    // setting up the request object now
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    [request setTimeoutInterval:60.0];
    [request setURL:[NSURL URLWithString:urlString]];
    [request setHTTPMethod:@"POST"];
    
    NSString *contentType = [NSString stringWithFormat:@"text/plain"];
    
    /*
     add some header info now
     we always need a boundary when we post a file
     also we need to set the content type
     
     You might want to generate a random boundary.. this is just the same
     as my output from wireshark on a valid html post
     */
    //NSString *boundary = [NSString stringWithString:@"---------------------------14737809831466499882746641449"];
    //NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@",boundary];
    
    [request addValue:contentType forHTTPHeaderField: @"Content-Type"];
    
    /*
     now lets create the body of the post
     */
    NSMutableData *body = [NSMutableData data];
    //[body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    //[body appendData:[[NSString stringWithString:@"Content-Disposition: form-data; name=\"userfile\"; filename=\"ipodfile.jpg\"\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
    //[body appendData:[[NSString stringWithString:@"Content-Type: application/octet-stream\r\n\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[NSData dataWithData:imageData]];
    //[body appendData:[[NSString stringWithFormat:@"\r\n--%@--\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    // setting the body of the post to the reqeust
    [request setHTTPBody:body];
    
    // now lets make the connection to the web
    NSData *returnData = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:nil];
    NSString *returnString = [[NSString alloc] initWithData:returnData encoding:NSUTF8StringEncoding];
    
    NSLog(@"%@",returnString);
    return YES;
}

@end
