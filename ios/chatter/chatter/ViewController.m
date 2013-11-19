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
@synthesize navBar;

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    chatQueue = [[NSMutableArray alloc] init];
    [self registerForKeyboardNotifications];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    ViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];
    
    // CSS for table cells
    pageCSS = @"body { margin:0; padding:1; }";
    cellMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:left; vertical-align:text-top; margin:0; padding:0 }";
    handleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#D0D0D0 }";
}

// This is called whenever the view is loaded through storyboard segues
- (id)initWithCoder:(NSCoder*)coder
{
    NSLog(@"initwithcoder vc");
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Give the table view rounded corners
    mTableView.layer.cornerRadius = 5;
    mTableView.layer.masksToBounds = YES;
    mTableView.backgroundView = nil;
    mTableView.backgroundColor = [UIColor groupTableViewBackgroundColor];
//    [mTableView setEditing:YES animated:YES];

    
    // Make the return key say 'Send'
    userInputTextField.returnKeyType = UIReturnKeySend;
}

- (void)viewWillAppear:(BOOL)animated
{
    navBar.topItem.title = _chatTitle;
}


- (void)viewWillDisappear:(BOOL)animated
{
    if ([self isBeingDismissed]) {
        [connection removeCallbackBlockFromSender:NSStringFromClass([self class])];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Other UI behaviors

- (void)tappedCell:(id)sender
{
    NSLog(@"double tapped cell: %d",swipedCellIndex);
//    [mTableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:swipedCellIndex inSection:0] animated:NO scrollPosition:UITableViewScrollPositionNone];
    // Make cell flash
    UITableViewCell* cell = [mTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:swipedCellIndex inSection:0]];
    UIView* wv = (UIView*)[cell.contentView viewWithTag:3];

    
    [UIView animateWithDuration:0.2 delay:0.0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut animations:^
    {
        [cell setHighlighted:YES animated:YES];
        [wv setBackgroundColor:[UIColor blueColor]];
    } completion:^(BOOL finished)
    {
        [UIView animateWithDuration:0.2 delay:0.0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut animations:^
         {
             NSLog(@"here");
             [cell setHighlighted:NO animated:NO];
             [wv setBackgroundColor:[UIColor clearColor]];
         } completion: NULL];
    }];

    // Send upvote
}

- (void)swipedCellRight:(id)sender
{
    NSLog(@"swiped cell: %d",swipedCellIndex);
    NSIndexPath* cellPath = [NSIndexPath indexPathForRow:swipedCellIndex inSection:0];
    NSArray *deleteIndexPath = [[NSArray alloc] initWithObjects:cellPath, nil];

    // Remove cell
    [mTableView beginUpdates];
    [mTableView deleteRowsAtIndexPaths:deleteIndexPath withRowAnimation:UITableViewRowAnimationRight];
    [chatQueue removeObjectAtIndex:cellPath.row];
    [mTableView endUpdates];
    
    // Send downvote
}

- (void)swipedCellLeft:(id)sender
{
    NSLog(@"swiped cell: %d",swipedCellIndex);
    NSIndexPath* cellPath = [NSIndexPath indexPathForRow:swipedCellIndex inSection:0];
    NSArray *deleteIndexPath = [[NSArray alloc] initWithObjects:cellPath, nil];
    
    // Remove cell
    [mTableView beginUpdates];
    [mTableView deleteRowsAtIndexPaths:deleteIndexPath withRowAnimation:UITableViewRowAnimationLeft];
    [chatQueue removeObjectAtIndex:cellPath.row];
    [mTableView endUpdates];
    
    // Send downvote
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
            NSLog(@"num msgs: %d",[chatQueue count]);
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

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    swipedCellIndex = indexPath.row;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleNone;
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

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
    static NSString *CellIdentifier = @"ChatCell";
    const int WEBVIEW_TAG = 1;
    const int ICONVIEW_TAG = 2;
    const int HIDDEN_VIEW = 3;
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    UIWebView* webview;
    UIImageView* iconView;
    UIView* hiddenView;
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.userInteractionEnabled = YES;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        // Add the icon view
        CGRect iviewFrame = CGRectMake(0, 0, 44, 44);
        iconView = [[UIImageView alloc] init];
        iconView.frame = iviewFrame;
        iconView.tag = ICONVIEW_TAG;
        iconView.opaque = NO;
        iconView.userInteractionEnabled = NO;
        [cell.contentView addSubview:iconView];
        
        // Add the webview
        webview = [[UIWebView alloc] init];
        webview.tag = WEBVIEW_TAG;
        CGRect wviewFrame = CGRectMake(44, 0, cell.contentView.bounds.size.width-44, cell.contentView.bounds.size.height);
        webview.frame = wviewFrame;
        webview.scrollView.scrollEnabled = NO;
        webview.userInteractionEnabled = NO;
        webview.opaque = YES;
        [cell.contentView addSubview:webview];
        
        // Add the hidden view
        hiddenView = [[UIView alloc] init];
        hiddenView.tag = HIDDEN_VIEW;
        hiddenView.opaque = NO;
        hiddenView.backgroundColor = [UIColor clearColor];
        hiddenView.alpha = 0.25;
        hiddenView.frame = cell.contentView.bounds;
        hiddenView.userInteractionEnabled = NO;
        [cell.contentView addSubview:hiddenView];
        
        // Add voting gestures to the cell
        UITapGestureRecognizer* tapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tappedCell:)];
        tapped.numberOfTapsRequired = 2;
        [cell addGestureRecognizer:tapped];
        UISwipeGestureRecognizer* swipedLeft = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipedCellLeft:)];
        swipedLeft.direction = UISwipeGestureRecognizerDirectionLeft;
        [cell addGestureRecognizer:swipedLeft];
        UISwipeGestureRecognizer* swipedRight = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipedCellRight:)];
        swipedRight.direction = UISwipeGestureRecognizerDirectionRight;
        [cell addGestureRecognizer:swipedRight];
    }
    else {
        webview = (UIWebView*)[cell.contentView viewWithTag:WEBVIEW_TAG];
        iconView = (UIImageView*)[cell.contentView viewWithTag:ICONVIEW_TAG];
        hiddenView = (UIView*)[cell.contentView viewWithTag:HIDDEN_VIEW];
    }
    
    MessageMessage* msg = [chatQueue objectAtIndex:indexPath.row];
    iconView.image = [UIImage imageNamed:@"default-icon.png"];
    if (msg != nil) {
        // TODO: image view
        NSString* html = [NSString stringWithFormat:@"<html><head><style> %@ %@ %@ </style></head><body><div class=msg>%@</div><div class=handle>%@</div></body></html>",pageCSS,cellMsgCSS,handleCSS,msg.message,msg.senderHandle];
        [webview loadHTMLString:html baseURL:nil];
    }
    
    cell.layer.cornerRadius = 5;
    cell.layer.masksToBounds = YES;
    return cell;
}


#pragma mark - Blurred Snapshot & Segue

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
    
    LeaveChatroomMessage* msg = [[LeaveChatroomMessage alloc] init];
    msg.chatroomId = ud.chatroomId;
    msg.userId = ud.userId;
    [connection sendMessage:msg];
}

@end
