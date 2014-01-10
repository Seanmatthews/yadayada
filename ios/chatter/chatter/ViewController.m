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
#import "ChatroomMessageCell.h"


const int MESSAGE_NUM_THRESH = 50;
const int MESSAGE_CHAR_LIMIT = 200;


@interface ViewController ()

@end

@implementation ViewController

//@synthesize userHandle;
@synthesize userInputTextField;
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
    selfMsgCSS = @"div.msg { font:12px/13px baskerville,serif; color:#004C3D; text-align:right; vertical-align:text-top; margin:0; padding:0 }";
    selfHandleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#D0D0D0; text-align:right }";
}

// This is called whenever the view is loaded through storyboard segues
- (id)initWithCoder:(NSCoder*)coder
{
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
    
    mTableView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
    
    // Make the return key say 'Send'
    userInputTextField.returnKeyType = UIReturnKeySend;
    
    // Pass chatId to UserDetails, which will use it to
    // save which chatrooms the user was joined to when
    // the app enters the background
    ud.chatroomId = _chatId;
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
    // Make cell flash
    UITableViewCell* cell = [mTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:swipedCellIndex inSection:0]];
    UIView* wv = (UIView*)[cell.contentView viewWithTag:3];

    // Send upvote
    MessageMessage* msg = [chatQueue objectAtIndex:swipedCellIndex];
    [self upvote:YES user:msg.senderId becauseOfMessage:msg.messageId];
    
    [UIView animateWithDuration:0.2 delay:0.0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut animations:^
    {
        [cell setHighlighted:YES animated:YES];
        [wv setBackgroundColor:[UIColor blueColor]];
    } completion:^(BOOL finished)
    {
        [UIView animateWithDuration:0.2 delay:0.0 options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut animations:^
         {
             [cell setHighlighted:NO animated:NO];
             [wv setBackgroundColor:[UIColor clearColor]];
         } completion: NULL];
    }];
}

- (void)swipeCell:(UITableViewRowAnimation)animation
{
    NSLog(@"swiped cell: %d",swipedCellIndex);
    NSIndexPath* cellPath = [NSIndexPath indexPathForRow:swipedCellIndex inSection:0];
    NSArray *deleteIndexPath = [[NSArray alloc] initWithObjects:cellPath, nil];
    
    // Send downvote
    MessageMessage* msg = [chatQueue objectAtIndex:cellPath.row];
    [self upvote:NO user:msg.senderId becauseOfMessage:msg.messageId];
    
    // Remove cell
    [mTableView beginUpdates];
    [mTableView deleteRowsAtIndexPaths:deleteIndexPath withRowAnimation:animation];
    [chatQueue removeObjectAtIndex:cellPath.row];
    [mTableView endUpdates];
}

- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId
{
    VoteMessage* msg = [[VoteMessage alloc] init];
    msg.chatroomId = _chatId;
    msg.voterId = ud.userId;
    msg.votedId = theirId;
    msg.msgId = msgId;
    if (upvote) {
        msg.upvote = 1;
    }
    else {
        msg.upvote = 0;
    }
    [connection sendMessage:msg];
}

- (void)swipedCellRight:(id)sender
{
    [self swipeCell:UITableViewRowAnimationRight];
}

- (void)swipedCellLeft:(id)sender
{
    [self swipeCell:UITableViewRowAnimationLeft];
}


#pragma mark - Keyboard Interaction + UITextFieldDelegate


- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillBeHidden:) name:UIKeyboardWillHideNotification object:nil];
}

// Called when the UIKeyboardDidShowNotification is sent.
- (void)keyboardWasShown:(NSNotification*)aNotification
{
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    // Animate the current view out of the way
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:0.3]; // if you want to slide up the view
    CGRect rect = self.view.frame;
    rect.origin.y -= kbSize.height;
    self.view.frame = rect;
    [UIView commitAnimations];
}

// Called when the UIKeyboardWillHideNotification is sent
- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    // Animate the current view back to where it was
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:0.3]; // if you want to slide up the view
    CGRect rect = self.view.frame;
    rect.origin.y += kbSize.height;
    self.view.frame = rect;
    [UIView commitAnimations];
}


- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    NSString* text = [textField text];
    if ([text length] > 0) {
        SubmitMessageMessage* sm = [[SubmitMessageMessage alloc] init];
        sm.userId = ud.userId;
        sm.chatroomId = _chatId;
        sm.message = text;
        
//        NSLog(@"%lli, %lli, %@",sm.userId,sm.chatroomId,sm.message);
        
        [connection sendMessage:sm];
    }
    [textField setText:@""];
    [textField resignFirstResponder];
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSString *newString = [textField.text stringByReplacingCharactersInRange:range withString:string];
    if (newString.length >= MESSAGE_CHAR_LIMIT) {
        // Send message automatically
        SubmitMessageMessage* sm = [[SubmitMessageMessage alloc] init];
        sm.userId = ud.userId;
        sm.chatroomId = _chatId;
        sm.message = newString;
        [textField setText:@""];
        [connection sendMessage:sm];
    }
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
    }
}

- (void)receivedMessage:(MessageMessage*) message
{
    [chatQueue addObject:message];
    
    if ([chatQueue count] > MESSAGE_NUM_THRESH) {
        [chatQueue removeObjectAtIndex:0];
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

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    MessageMessage* msg = [chatQueue objectAtIndex:indexPath.row];
    return [ChatroomMessageCell heightForText:msg.message];
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
    static NSString *CellIdentifier = @"ChatroomMessageCell";
    ChatroomMessageCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    MessageMessage* msg = [chatQueue objectAtIndex:indexPath.row];
    
    if (nil == cell) {
        cell = [[ChatroomMessageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        
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
    [cell setUserHandle:msg.senderHandle];
    [cell setMessage:msg.message];
    [cell setUserIcon:[UIImage imageNamed:@"default-icon.png"]];
    [cell setSelfMessage:(msg.senderId == ud.userId ? YES : NO)];
    [cell arrangeElements];
    
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
    msg.chatroomId = _chatId;
    msg.userId = ud.userId;
    [connection sendMessage:msg];
}

@end
