//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "Messages.h"
#import "ChatroomMessageCell.h"
#import "SettingsViewController.h"
#import "UIInviteAlertView.h"


//const int MESSAGE_NUM_THRESH = 50;


@interface ViewController ()

@end

@implementation ViewController

@synthesize userInputTextField;
@synthesize mTableView;
@synthesize navBar;

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    contacts = [Contacts sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    [self registerForKeyboardNotifications];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [Connection sharedInstance];
    
    // CSS for table cells
    pageCSS = @"body { margin:0; padding:1; }";
    cellMsgCSS = @"div.msg { font:13px/14px baskerville,serif; color:#004C3D; text-align:left; vertical-align:text-top; margin:0; padding:0 }";
    handleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#DADADA }";
    selfMsgCSS = @"div.msg { font:13px/14px baskerville,serif; color:#004C3D; text-align:right; vertical-align:text-top; margin:0; padding:0 }";
    selfHandleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#DADADA; text-align:right }";
    
    ViewController* __weak weakSelf = self;
    [connection performSelector:@selector(addCallbackBlock:fromSender:) withObject:^(MessageBase* m) {
                dispatch_group_async(connection.parseGroup, connection.parseQueue, ^{
                    [weakSelf messageCallback:m];
                });
          } withObject:NSStringFromClass([self class])];
    
    // Add us to the invite notification
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(invitedToChatroom:)
                                                 name:@"InviteNotification"
                                               object:nil];
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

-(void)viewDidAppear:(BOOL)animated
{

}

- (void)viewWillDisappear:(BOOL)animated
{

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Other UI behaviors

- (void)tappedCell:(id)sender
{
    // Make cell flash
    UITableViewCell* cell = [mTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:swipedCellIndex inSection:0]];
    UIView* wv = (UIView*)[cell.contentView viewWithTag:3];

    // Send upvote
    MessageMessage* msg = [[chatManager currentChatQueue] objectAtIndex:swipedCellIndex];
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
    NSIndexPath* cellPath = [NSIndexPath indexPathForRow:swipedCellIndex inSection:0];
    NSArray *deleteIndexPath = [[NSArray alloc] initWithObjects:cellPath, nil];
    
    // Send downvote
    MessageMessage* msg = [[chatManager currentChatQueue] objectAtIndex:cellPath.row];
    [self upvote:NO user:msg.senderId becauseOfMessage:msg.messageId];
    
    // Remove cell
    [mTableView beginUpdates];
    [mTableView deleteRowsAtIndexPaths:deleteIndexPath withRowAnimation:animation];
    [[chatManager currentChatQueue] removeObjectAtIndex:cellPath.row];
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
        [connection sendMessage:sm];
    }
    [textField setText:@""];
    [textField resignFirstResponder];
    return YES;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    NSString *newString = [textField.text stringByReplacingCharactersInRange:range withString:string];
    if (newString.length >= [ChatroomMessageCell getMessageCharLimit]) {
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

- (void)refreshMessages
{
//    [mTableView reloadData];
    NSUInteger numMessages = [[chatManager currentChatQueue] count];
    NSIndexPath* ipath = [NSIndexPath indexPathForRow:[[chatManager currentChatQueue] count]-1 inSection:0];
    
    // Only reload the newest cell
    [mTableView beginUpdates];
    if (numMessages == chatManager.MESSAGE_NUM_THRESH &&
        [mTableView numberOfRowsInSection:0] == chatManager.MESSAGE_NUM_THRESH) {
        
        NSIndexPath* delPath = [NSIndexPath indexPathForRow:0 inSection:0];
        [mTableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:delPath] withRowAnimation:NO];
    }
    [mTableView insertRowsAtIndexPaths:[NSArray arrayWithObject:ipath] withRowAnimation:UITableViewRowAnimationNone];
    [mTableView endUpdates];
    [mTableView scrollToRowAtIndexPath:ipath atScrollPosition:UITableViewScrollPositionBottom animated:NO];
}

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case InviteUserSuccess:
            // TODO: add message in the midst of chat messages
            break;
            
        case InviteUserReject:
            // TODO: add message in the midst of chat messages
            break;
            
        case Message:
            NSLog(@"Message");
            if (((MessageMessage*)message).chatroomId == _chatId) {
                dispatch_group_notify(connection.parseGroup, connection.parseQueue, ^{
                    [self performSelectorOnMainThread:@selector(refreshMessages) withObject:nil waitUntilDone:NO];
                    // TODO: try making the dispatch queue on the main thread
                });
            }
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

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    MessageMessage* msg = [[chatManager currentChatQueue] objectAtIndex:indexPath.row];
//    NSLog(@"height %f",[ChatroomMessageCell heightForText:msg.message]);
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
    return [[chatManager currentChatQueue] count];
}

// This function is for recovering cells, or initializing a new one.
// It is not for filling in cell data.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    id rootMsg = [[chatManager currentChatQueue] objectAtIndex:indexPath.row];
    NSString *CellIdentifier = [NSString stringWithFormat:@"%ld_%ld",(long)indexPath.section,(long)indexPath.row];
    
    if ([rootMsg isMemberOfClass:[MessageMessage class]]) {
    
        MessageMessage* msg = [[chatManager currentChatQueue] objectAtIndex:indexPath.row];
//        NSString *CellIdentifier = [NSString stringWithFormat:@"%ld_%ld",(long)indexPath.section,(long)indexPath.row];
        ChatroomMessageCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
        
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
        [cell setUserIcon:nil];
        [cell setSelfMessage:(msg.senderId == ud.userId ? YES : NO)];
        [cell arrangeElements];
        
        return cell;
    }
    else {
        UITableViewCell* cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.backgroundColor = [UIColor clearColor];
        cell.textLabel.font = [UIFont fontWithName:@"System" size:13.];
        cell.textLabel.textColor = [UIColor lightTextColor];
        
        if ([rootMsg isMemberOfClass:[JoinedChatroomMessage class]]) {
            NSString* userHandle = [(JoinedChatroomMessage*)rootMsg userHandle];
            cell.textLabel.text = [NSString stringWithFormat:@"%@ joined.",userHandle];
        }
        else if ([rootMsg isMemberOfClass:[LeftChatroomMessage class]]) {
            NSString* userHandle = [(LeftChatroomMessage*)rootMsg userHandle];
            cell.textLabel.text = [NSString stringWithFormat:@"%@ left.",userHandle];
        }
        return cell;
    }
}


#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"unwindToChatList"]) {
        
    }
}

- (IBAction)unwindToChatroom:(UIStoryboardSegue*)unwindSegue
{
    if ([contacts invitedContact]) {
        InviteUserMessage* ium = [[InviteUserMessage alloc] init];
        ium.senderId = ud.userId;
        ium.senderHandle = ud.handle;
        ium.chatroomId = _chatId;
        ium.recipientId = 0;
        ium.recipientPhoneNumber = [[[contacts invitedContact] getPhoneNumber] longLongValue];
        ium.chatroomName = @"NA";
        [connection sendMessage:ium];
        [contacts setInvitedContact:nil];
        NSLog(@"phone: %lld",ium.recipientPhoneNumber);
    }
}

- (void)invitedToChatroom:(NSNotification*)notification
{
    NSLog(@"invited to chatroom, going");
    [self performSegueWithIdentifier:@"unwindToChatList" sender:nil];
}


#pragma mark - ABPeoplePickerNavigationControllerDelegate

- (void)peoplePickerNavigationControllerDidCancel:(ABPeoplePickerNavigationController *)peoplePicker
{
    // Do nothing
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (BOOL)peoplePickerNavigationController:(ABPeoplePickerNavigationController *)peoplePicker shouldContinueAfterSelectingPerson:(ABRecordRef)person
{
    [self dismissViewControllerAnimated:YES completion:nil];
    if (person) {
        InviteUserMessage* ium = [[InviteUserMessage alloc] init];
        ium.chatroomId = [chatManager currentChatroomId];
        ium.senderId = ud.userId;
        ium.recipientId = 0;
        ium.recipientPhoneNumber = [[contacts iPhoneNumberForRecord:person] longLongValue];
        
        if (!ium.recipientPhoneNumber) {
            UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"Woops!" message:@"Contact does not have iPhone" delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alert show];
        }
        else {
            [connection sendMessage:ium];
        }
    }
    return NO;
}

- (BOOL)peoplePickerNavigationController:(ABPeoplePickerNavigationController *)peoplePicker
      shouldContinueAfterSelectingPerson:(ABRecordRef)person
                                property:(ABPropertyID)property
                              identifier:(ABMultiValueIdentifier)identifier
{
    return NO;
}



@end
