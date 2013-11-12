//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "Messages.h"
#import "UIImage+ImageEffects.h"
#import <QuartzCore/QuartzCore.h>

const int MESSAGE_NUM_THRESH = 50;


@interface ViewController ()

@end

@implementation ViewController

//@synthesize userHandle;
@synthesize userInputTextField;
@synthesize scrollView;
@synthesize mTableView;


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    ud = [UserDetails sharedInstance];
    chatQueue = [[NSMutableArray alloc] init];
    
    // Give the table view rounded corners
    mTableView.layer.cornerRadius = 5;
    mTableView.layer.masksToBounds = YES;
    
    userInputTextField.returnKeyType = UIReturnKeySend;
    [self registerForKeyboardNotifications];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    ViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Other UI behaviors

- (void)tappedCell:(id)sender
{
    NSLog(@"double tapped cell");
}

- (void)swipedCell:(id)sender
{
    NSLog(@"swiped cell");
}

- (void)receivedMessage:(MessageMessage*) message
{
    [chatQueue addObject:message];
    
    if ([chatQueue count] > MESSAGE_NUM_THRESH) {
        [chatQueue removeObjectAtIndex:0];
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
    //UIScrollView* scrollView = (UIScrollView*)[self view];
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    CGRect bkgndRect = userInputTextField.superview.frame;
    // TODO: figure out why this works and the normal way doesn't
    kbSize.height += 100;
    bkgndRect.size.height += kbSize.height;
    [userInputTextField.superview setFrame:bkgndRect];
    [scrollView setContentOffset:CGPointMake(0.0, userInputTextField.frame.origin.y-kbSize.height) animated:YES];
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    CGRect bkgndRect = userInputTextField.superview.frame;
    bkgndRect.size.height -= kbSize.height;
    [userInputTextField.superview setFrame:bkgndRect];
    [scrollView setContentOffset:CGPointMake(0.0, 0.0) animated:YES];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    NSString* text = [textField text];
    if ([text length] > 0) {
        SubmitMessageMessage* sm = [[SubmitMessageMessage alloc] init];
        sm.userId = ud.userId;
        sm.chatroomId = ud.chatroomId;
        sm.message = text;
        
        NSLog(@"%lli, %lli, %@",sm.userId,sm.chatroomId,sm.message);
        
        [connection sendMessage:sm];
    }
    [textField setText:@""];
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - incoming and outgoing messages


- (void)messageCallback:(MessageBase*)message
{
    NSIndexPath* ipath;
    switch (message.type) {
            
        case Message:
            NSLog(@"Message");
            [self receivedMessage:(MessageMessage*)message];
            [mTableView reloadData];
            ipath = [NSIndexPath indexPathForRow:[chatQueue count]-1 inSection:0];
            [mTableView scrollToRowAtIndexPath:ipath atScrollPosition:UITableViewScrollPositionBottom animated:YES];
            break;
            
        case JoinedChatroom:
            NSLog(@"Joined Chatroom");
            break;
            
        case LeftChatroom:
            NSLog(@"Left Chatroom");
            break;
    }
}

#pragma mark - UITableViewDelegate methods

//- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    return 0.0;
//}


#pragma mark - UITableViewDataSource methods

//- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
//{
//    return 1;
//}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // There will only ever be one section for a table.
    // TODO: alter this behavior for multiple chatrooms
    return [chatQueue count];
}

// This function is for recovering cells, or initializing a new one.
// It is not for filling in cell data.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    const int WEBVIEW_TAG = 1;
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    UIWebView* webview;
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.userInteractionEnabled = YES;
        
        
        // Add voting gestures to the cell
        UITapGestureRecognizer* tapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tappedCell:)];
        tapped.numberOfTapsRequired = 2;
        [cell addGestureRecognizer:tapped];
        UISwipeGestureRecognizer* swiped = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipedCell:)];
        swiped.direction = UISwipeGestureRecognizerDirectionLeft | UISwipeGestureRecognizerDirectionRight;
        [cell addGestureRecognizer:swiped];
        
        // TODO: where to get icon size from? ConnectAccept message?
        cell.imageView.frame = CGRectMake(0, 0, 53, 53);
        
        webview = [[UIWebView alloc] init];
        webview.tag = WEBVIEW_TAG;
        // does this fill the content view?
        webview.frame = cell.contentView.bounds;
        webview.scrollView.scrollEnabled = NO;
        [cell.contentView addSubview:webview];
    }
    else {
        webview = (UIWebView*)[cell.contentView viewWithTag:WEBVIEW_TAG];
    }
    
    MessageMessage* msg = [chatQueue objectAtIndex:indexPath.row];
    if (msg != nil) {
        // TODO: image view
        NSString* html = msg.message;
        [webview loadHTMLString:html baseURL:nil];
    }
    
    return cell;
}

/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */

/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
 {
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
 }
 else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
 {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */

/*
 #pragma mark - Navigation
 
 // In a story board-based application, you will often want to do a little preparation before navigation
 - (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
 {
 // Get the new view controller using [segue destinationViewController].
 // Pass the selected object to the new view controller.
 }
 
 */


#pragma mark - Blurred Snapshot

- (UIImage*)blurredSnapshot
{
    // Create the image context
    UIGraphicsBeginImageContextWithOptions(self.view.bounds.size, YES, self.view.window.screen.scale);
    
    // There he is! The new API method
    [self.view drawViewHierarchyInRect:self.view.frame afterScreenUpdates:NO];
    
    // Get the snapshot
    UIImage *snapshotImage = UIGraphicsGetImageFromCurrentImageContext();
    //[UIImage imageNamed:@"Default@2x.png"];
    
    // Now apply the blur effect using Apple's UIImageEffect category
    UIImage *blurredSnapshotImage = [snapshotImage applyLightEffect];
    
    // Or apply any other effects available in "UIImage+ImageEffects.h"
    //UIImage *blurredSnapshotImage = [snapshotImage applyDarkEffect];
    //UIImage *blurredSnapshotImage = [snapshotImage applyExtraLightEffect];
    
    // Be nice and clean your mess up
    UIGraphicsEndImageContext();
    
    return blurredSnapshotImage;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    MenuViewController* vc = (MenuViewController*)segue.destinationViewController;
    vc.image =[self blurredSnapshot];
}

@end
