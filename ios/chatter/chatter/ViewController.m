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

- (void)initCode;
- (void)refreshView:(NSNotification*)notification;
- (void)swipeCell:(ChatroomMessageCell*)cell withAnimation:(UITableViewRowAnimation)animation;
- (void)swipedCellLeft:(UIGestureRecognizer*)sender;
- (void)swipedCellRight:(UIGestureRecognizer*)sender;
- (void)tappedCell:(id)sender;
- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId;

- (void)keyboardWasShown:(NSNotification*)aNotification;
- (void)keyboardWillBeHidden:(NSNotification*)aNotification;
- (void)receivedInviteUserSuccess:(NSNotification*)notification;
- (void)receivedInviteUserReject:(NSNotification*)notification;
- (void)segueToChatroom:(NSNotification*)notification;
- (void)registerForNotifications;
- (void)unregisterForNotifications;
- (void)addMessageAtIndexPath;
- (void)initMessageCell:(ChatroomMessageCell*)cell withReuseIdentifier:(NSString*)reuseId;

@end

@implementation ViewController
{
    UserDetails* ud;
    Connection* connection;
    Contacts* contacts;
    NSThread* connectionThread;
    NSString* cellMsgCSS;
    NSString* handleCSS;
    NSString* pageCSS;
    NSString* selfMsgCSS;
    NSString* selfHandleCSS;
    ChatroomManagement* chatManager;
    NSMutableArray* inviteAlerts;
    NSMutableArray* indexPathsToDisplay;
    NSMutableArray* displayedIndexPaths;
}

@synthesize userInputTextField;
@synthesize mTableView;
@synthesize navBar;

- (void)initCode
{
    ud = [UserDetails sharedInstance];
    contacts = [Contacts sharedInstance];
    chatManager = [ChatroomManagement sharedInstance];
    connection = [Connection sharedInstance];
    indexPathsToDisplay = [[NSMutableArray alloc] init];
    displayedIndexPaths = [[NSMutableArray alloc] init];
    
    // CSS for table cells
    pageCSS = @"body { margin:0; padding:1; }";
    cellMsgCSS = @"div.msg { font:13px/14px baskerville,serif; color:#004C3D; text-align:left; vertical-align:text-top; margin:0; padding:0 }";
    handleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#DADADA }";
    selfMsgCSS = @"div.msg { font:13px/14px baskerville,serif; color:#004C3D; text-align:right; vertical-align:text-top; margin:0; padding:0 }";
    selfHandleCSS = @"div.handle { font:11px/12px baskerville,serif; color:#DADADA; text-align:right }";
    
    [NSTimer scheduledTimerWithTimeInterval:0.25
                                     target:self
                                   selector:@selector(addMessageAtIndexPath)
                                   userInfo:nil
                                    repeats:YES];
}

// This is called whenever the view is loaded through storyboard segues
- (id)initWithCoder:(NSCoder*)coder
{
    if (self = [super initWithCoder:coder]) {
        [self initCode];
    }
    return self;
}

- (void)registerForNotifications
{
    // TODO: get rid of JoinedChatroom, LeftChatroom, and Message, then
    // subscribe to notifications coming from the chatroom, which will post
    // new messages to its descriptor name.
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(refreshView:)
                                                 name:NSStringFromClass([self class])
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWasShown:)
                                                 name:UIKeyboardWillShowNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(segueToChatroom:)
                                                 name:@"segueToChatroomNotification"
                                               object:nil];
    
    for (NSString* notificationName in @[@"InviteUserSuccess", @"InviteUserReject"]) {
        NSString* selectorName = [NSString stringWithFormat:@"received%@:",notificationName];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:NSSelectorFromString(selectorName)
                                                     name:[NSString stringWithFormat:@"%@Message",notificationName]
                                                   object:nil];
    }
}

- (void)unregisterForNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
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
    
    userInputTextField.returnKeyType = UIReturnKeySend;
    
    mTableView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self registerForNotifications];
    [self refreshView:nil];

//    navBar.topItem.title = _chatroom.chatroomName;
//
//    // Load all messages from from the chatmanager queue when we come into view
//    [displayedIndexPaths removeAllObjects];
//    [displayedIndexPaths addObjectsFromArray:[_chatroom chatQueue]];
//    [mTableView reloadData];
    
    // setup KVO with chatqueue
    [_chatroom addObserver:self
                forKeyPath:@"chatQueue"
                   options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld)
                   context:nil];
}

- (void)viewDidAppear:(BOOL)animated
{
//    NSLog(@"chat view did appear");
    [[NSNotificationCenter defaultCenter] postNotificationName:@"viewDidAppear" object:self];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self unregisterForNotifications];
    
    // Remove KVO
    [_chatroom removeObserver:self forKeyPath:@"chatQueue"];
}

- (void)refreshView:(NSNotification*)notification
{
    NSLog(@"[ViewController] Refreshing, the view");
    navBar.topItem.title = _chatroom.chatroomName;
    
    
    // Load all messages from from the chatmanager queue when we come into view
    [displayedIndexPaths removeAllObjects];
    [displayedIndexPaths addObjectsFromArray:[_chatroom chatQueue]];
    [mTableView reloadData];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar {
    return UIBarPositionTopAttached;
}


#pragma mark - Voting UI

- (void)tappedCell:(UIGestureRecognizer*)sender
{
    ChatroomMessageCell* tappedCell = (ChatroomMessageCell*)sender.view;
    NSInteger row = [_chatroom.chatQueue indexOfObject:tappedCell.messageObj];
    NSLog(@"tapped %ld",(long)row);

    // Make cell flash & Send upvote
    if (row < [[_chatroom chatQueue] count]) {
        id aMsg = [[_chatroom chatQueue] objectAtIndex:row];
        if ([aMsg isMemberOfClass:[MessageMessage class]]) {
            UIView* wv = (UIView*)[tappedCell.contentView viewWithTag:3];
            MessageMessage* msg = aMsg;
            [self upvote:YES user:msg.senderId becauseOfMessage:msg.messageId];
            
            [UIView animateWithDuration:0.2
                                  delay:0.0
                                options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut
                             animations:^{
                                [tappedCell setHighlighted:YES animated:YES];
                                [wv setBackgroundColor:[UIColor blueColor]];
                             }
                             completion:^(BOOL finished) {
                                 [UIView animateWithDuration:0.2
                                                       delay:0.0
                                                     options:UIViewAnimationOptionAllowUserInteraction|UIViewAnimationOptionCurveEaseInOut
                                                  animations:^{
                                                     [tappedCell setHighlighted:NO animated:NO];
                                                     [wv setBackgroundColor:[UIColor clearColor]];
                             } completion: NULL];
            }];
        }
    }
}

- (void)swipeCell:(ChatroomMessageCell*)cell withAnimation:(UITableViewRowAnimation)animation
{
    NSInteger row = [_chatroom.chatQueue indexOfObject:cell.messageObj];
    
    // Send downvote
    if (row < [[_chatroom chatQueue] count]) {
        id aMsg = [[_chatroom chatQueue] objectAtIndex:row];
        if ([aMsg isMemberOfClass:[MessageMessage class]]) {
            NSIndexPath* cellPath = [NSIndexPath indexPathForRow:row inSection:0];
            NSArray *deleteIndexPath = [[NSArray alloc] initWithObjects:cellPath, nil];
            MessageMessage* msg = [[_chatroom chatQueue] objectAtIndex:row];
            [self upvote:NO user:msg.senderId becauseOfMessage:msg.messageId];
            
            // Remove cell
            [mTableView beginUpdates];
            [mTableView deleteRowsAtIndexPaths:deleteIndexPath withRowAnimation:animation];
            [[_chatroom chatQueue] removeObjectAtIndex:row];
            [mTableView endUpdates];
        }
    }
}

- (void)upvote:(BOOL)upvote user:(long long)theirId becauseOfMessage:(long long)msgId
{
    VoteMessage* msg = [[VoteMessage alloc] init];
    msg.chatroomId = [_chatroom.cid longLongValue];
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

- (void)swipedCellRight:(UIGestureRecognizer*)sender
{
    [self swipeCell:(ChatroomMessageCell*)sender.view withAnimation:UITableViewRowAnimationRight];
}

- (void)swipedCellLeft:(UIGestureRecognizer*)sender
{
    [self swipeCell:(ChatroomMessageCell*)sender.view withAnimation:UITableViewRowAnimationLeft];
}


#pragma mark - Keyboard Interaction + UITextFieldDelegate


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
        sm.chatroomId = [_chatroom.cid longLongValue];
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
        sm.chatroomId = [_chatroom.cid longLongValue];
        sm.message = newString;
        [textField setText:@""];
        [connection sendMessage:sm];
    }
    return YES;
}


#pragma mark - incoming and outgoing messages

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqual:@"chatQueue"]) {
        NSLog(@"new chat %lu", (unsigned long)[[object chatQueue] count]);
//        [change objectForKey:NSKeyValueChangeNewKey];
        NSIndexPath* ipath = [NSIndexPath indexPathForRow:([[object chatQueue] count]-1) inSection:0];
        //[self addMessageAtIndexPath:ipath];
        [indexPathsToDisplay addObject:ipath];
        [displayedIndexPaths addObject:ipath];
        
        while ([displayedIndexPaths count] > _chatroom.MESSAGE_NUM_THRESH) {
            [displayedIndexPaths removeObjectAtIndex:0];
        }
    }
}

//- (void)addMessageAtIndexPath:(NSIndexPath*)indexPath
- (void)addMessageAtIndexPath
{
    if ([indexPathsToDisplay count] < 1) {
        return;
    }
    
    [CATransaction begin];
    NSIndexPath* path = [indexPathsToDisplay lastObject];
    [CATransaction setCompletionBlock:^{
        [mTableView scrollToRowAtIndexPath:path
                          atScrollPosition:UITableViewScrollPositionBottom animated:NO];
    }];
    
    [mTableView beginUpdates];
    NSInteger totalDisplayMessages = [displayedIndexPaths count] + [indexPathsToDisplay count];
    if (totalDisplayMessages > _chatroom.MESSAGE_NUM_THRESH) {
        NSMutableArray* deletePaths = [[NSMutableArray alloc] init];
        for (NSInteger i=0; i<totalDisplayMessages; ++i) {
            [deletePaths addObject:[NSIndexPath indexPathForRow:i inSection:0]];
        }
        [mTableView deleteRowsAtIndexPaths:deletePaths withRowAnimation:UITableViewRowAnimationNone];
    }
    [mTableView insertRowsAtIndexPaths:indexPathsToDisplay withRowAnimation:UITableViewRowAnimationNone];
    [mTableView endUpdates];
    [CATransaction commit];
    
    [indexPathsToDisplay removeAllObjects];
}

- (void)receivedInviteUserSuccess:(NSNotification*)notification
{
    // TODO: add message in the midst of chat messages, when inivitiation was a success
}

- (void)receivedInviteUserReject:(NSNotification*)notification
{
    // TODO: add message in the midst of chat messages, when invitation failed
}

- (void)segueToChatroom:(NSNotification*)notification
{
    _chatroom = notification.object;
    [self viewDidDisappear:NO];
    [self viewWillAppear:NO];
}


#pragma mark - UITableViewDelegate methods

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleNone;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    id msg = [[_chatroom chatQueue] objectAtIndex:indexPath.row];
    
    if ([msg isMemberOfClass:[MessageMessage class]]) {
        return [ChatroomMessageCell heightForText:[msg message]];
    }
    else { // Assume JoinedChatroom or LeftChatroom
        return 30.;
    }
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // There will only ever be one section for a table.
    return [displayedIndexPaths count];
//    return [[_chatroom chatQueue] count];
}

- (void)initMessageCell:(ChatroomMessageCell*)cell withReuseIdentifier:(NSString*)reuseId
{
    cell = [[ChatroomMessageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseId];
    
    // Add voting gestures to the cell
    UITapGestureRecognizer* tapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tappedCell:)];
    tapped.numberOfTapsRequired = 1;
    [cell addGestureRecognizer:tapped];
    UISwipeGestureRecognizer* swipedLeft = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipedCellLeft:)];
    swipedLeft.direction = UISwipeGestureRecognizerDirectionLeft;
    [cell addGestureRecognizer:swipedLeft];
    UISwipeGestureRecognizer* swipedRight = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipedCellRight:)];
    swipedRight.direction = UISwipeGestureRecognizerDirectionRight;
    [cell addGestureRecognizer:swipedRight];
}

// This function is for recovering cells, or initializing a new one.
// It is not for filling in cell data.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    id rootMsg = [[_chatroom chatQueue] objectAtIndex:indexPath.row];
    NSString *CellIdentifier = [NSString stringWithFormat:@"%ld_%ld",(long)indexPath.section,(long)indexPath.row];
    
    if ([rootMsg isMemberOfClass:[MessageMessage class]]) {
    
        MessageMessage* msg = [[_chatroom chatQueue] objectAtIndex:indexPath.row];
        ChatroomMessageCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
        
        if (nil == cell) {
            cell = [[ChatroomMessageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
            
            // Add voting gestures to the cell
            UITapGestureRecognizer* tapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tappedCell:)];
            tapped.numberOfTapsRequired = 1;
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
        [cell setMessageObj:msg];
        
        return cell;
    }
    else {
        UITableViewCell* cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.backgroundColor = [UIColor clearColor];
        cell.textLabel.font = [UIFont fontWithName:@"System" size:12.];
        cell.textLabel.textColor = [UIColor grayColor];
        cell.selected = NO;
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        
        if ([rootMsg isMemberOfClass:[JoinedChatroomMessage class]]) {
            NSString* userHandle = ((JoinedChatroomMessage*)rootMsg).userHandle;
            cell.textLabel.text = [NSString stringWithFormat:@"%@ joined.",userHandle];
        }
        else if ([rootMsg isMemberOfClass:[LeftChatroomMessage class]]) {
            NSString* userHandle = ((LeftChatroomMessage*)rootMsg).userHandle;
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
        ium.chatroomId = [_chatroom.cid longLongValue];
        ium.recipientId = 0;
        ium.recipientPhoneNumber = [[[contacts invitedContact] getPhoneNumber] longLongValue];
        ium.chatroomName = @"NA";
        [connection sendMessage:ium];
        [contacts setInvitedContact:nil];
        NSLog(@"phone: %lld",ium.recipientPhoneNumber);
    }
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
        ium.chatroomId = [_chatroom.cid longLongValue];
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
