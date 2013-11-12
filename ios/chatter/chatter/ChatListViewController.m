//
//  ChatListViewController.m
//  chatter
//
//  Created by sean matthews on 11/7/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ChatListViewController.h"
#import "UIImage+ImageEffects.h"
#import "MenuViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "ChatroomListCell.h"

@interface ChatListViewController ()

@end

@implementation ChatListViewController

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
	location = [Location sharedInstance];
    
    // Give the map view rounded corners
    _tableView.layer.cornerRadius = 5;
    _tableView.layer.masksToBounds = YES;
    
    chatroomList = [[NSMutableArray alloc] init];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - incoming and outgoing messages


- (void)messageCallback:(MessageBase*)message
{
    switch (message.type) {
        case Chatroom:
            [chatroomList addObject:(ChatroomMessage*)message];
            break;
            
        case JoinedChatroom:
            NSLog(@"Joined Chatroom");
            break;
            
        case LeftChatroom:
            NSLog(@"Left Chatroom");
            break;
    }
}


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
    
    // Be nice and clean your mess up
    UIGraphicsEndImageContext();
    
    return blurredSnapshotImage;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    MenuViewController* vc = (MenuViewController*)segue.destinationViewController;
    vc.image =[self blurredSnapshot];
}


#pragma mark - UITableViewDataSource methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Globals and Locals
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [chatroomList count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *CellIdentifier = @"ChatroomListCell";
    ChatroomListCell *cell = (ChatroomListCell*) [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
    if (cell == nil) {
        NSArray* topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ChatroomListCell" owner:self options:nil];
        for (id currentObject in topLevelObjects) {
            if ([currentObject isKindOfClass:[UITableViewCell class]]) {
                cell = (ChatroomListCell*)currentObject;
                break;
            }
        }
    }
    
    // Configure the cell.
    ChatroomMessage *chatroom = [chatroomList objectAtIndex:indexPath.row];
    
    // calculate miles from origin
    CLLocationCoordinate2D chatroomOrigin = CLLocationCoordinate2DMake(chatroom.latitude, chatroom.longitude);
    CLLocationCoordinate2D userOrigin = CLLocationCoordinate2DMake([location currentLat], [location currentLong]);
    CGFloat distance = [Location milesBetweenSource:chatroomOrigin andDestination:userOrigin];
    
    // TODO: get an image
    cell.chatroomImage.image = [[UIImage alloc] init];
    
    cell.chatroomName.text = chatroom.chatroomName;
    cell.milesFromOrigin.text = [NSString stringWithFormat:@"%f miles away",distance];
    cell.percentActive.text = [NSString stringWithFormat:@"%d% active",chatroom.chatActivity];
    cell.numberOfUsers.text = [NSString stringWithFormat:@"%d users",chatroom.numberOfUsers];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}

#pragma mark - UITableViewDelegate methods

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 55.0;
}

@end
