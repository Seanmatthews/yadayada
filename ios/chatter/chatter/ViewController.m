//
//  ViewController.m
//  chatter
//
//  Created by sean matthews on 10/22/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "Messages.h"



@interface ViewController ()

@end

@implementation ViewController

@synthesize userHandle;
@synthesize userInputTextField;
@synthesize scrollView;
@synthesize mTableView;


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"HasFinishedTutorial"]) {
        ud = [[UserDetails alloc] init];
    }
    else {
        // Getting here implies that the user has done the tutorial
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"HasFinishedTutorial"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        ud = [[UserDetails alloc] initWithHandle:userHandle];
    }
    
    userInputTextField.returnKeyType = UIReturnKeySend;
    [self registerForKeyboardNotifications];
    
    // Get connection object and add this controller's callback
    // method for incoming connections.
    connection = [[Connection alloc] init];
    [connection connect];
    ViewController* __weak weakSelf = self;
    [connection addCallbackBlock:^(MessageBase* m){ [weakSelf messageCallback:m];} fromSender:NSStringFromClass([self class])];

    // We need this because the run loops of connection don't work until
    // the view is completely loaded.
    [self performSelector:@selector(connectRegisterLogin) withObject:nil afterDelay:1.0];
    
    NSLog(@"handle: %@",userHandle);
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
        [connection sendMessage:sm];
    }
    [textField resignFirstResponder];
    return YES;
}


#pragma mark - incoming and outgoing messages

- (void)connectRegisterLogin
{
    NSLog(@"Going to try to connect now");
    
    ConnectMessage* cm = [[ConnectMessage alloc] init];
    // TODO: get api version programatically
    cm.APIVersion = 1;
    cm.UUID = ud.UUID;
    [self sendMessage:cm];
    
    RegisterMessage* rm = [[RegisterMessage alloc] init];
    rm.handle = @"sean";
    rm.userName = ud.UUID;
    rm.password = @"pass";
    [self sendMessage:rm];
    
    LoginMessage* lm = [[LoginMessage alloc] init];
    lm.userName = ud.UUID;
    lm.password = @"pass";
    [self sendMessage:lm];
}

- (void)sendMessage:(MessageBase*)message
{
    [connection sendMessage:message];
}

- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
            
        case RegisterAccept:
            NSLog(@"Register Accept");
            ud.userId = ((RegisterAcceptMessage*)message).userId;
            break;
            
        case RegisterReject:
            NSLog(@"Register Reject");
            NSLog(@"%@",((RegisterRejectMessage*)message).reason);
            break;
            
        case ConnectAccept:
            NSLog(@"Connect Accept");
            ud.chatroomId = ((ConnectAcceptMessage*)message).globalChatId;
            break;
            
        case ConnectReject:
            NSLog(@"Connect Reject");
            break;
            
        case LoginAccept:
            NSLog(@"Login Accept");
            break;
            
        case LoginReject:
            NSLog(@"Login Reject");
            break;
            
        case Message:
            NSLog(@"Message");
            break;
            
        case Chatroom:
            NSLog(@"Chatroom");
            break;
            
        case JoinChatroomReject:
            NSLog(@"Join Chatroom Reject");
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

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
#warning Incomplete method implementation.
    // Return the number of rows in the section.
    return 0;
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
        [cell.contentView addSubview:webview];
    }
    else {
        webview = (UIWebView*)[cell.contentView viewWithTag:WEBVIEW_TAG];
    }
    
    NSDictionary *aDict = [mTableView cellForRowAtIndexPath:indexPath.row];
    
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

@end
