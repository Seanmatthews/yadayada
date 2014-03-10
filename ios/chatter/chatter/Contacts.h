//
//  Contacts.h
//  chatter
//
//  Created by sean matthews on 2/16/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

@import Foundation;
@import AddressBook;
#import "Messages.h"


@interface Person : NSObject

- (id)initWithFirstName:(NSString*)fName lastName:(NSString*)lName phoneNumber:(NSNumber*)phoneNum;
- (NSString*)getFirstName;
- (NSString*)getLastName;
- (NSNumber*)getPhoneNumber;

@end


@interface Contacts : NSObject

@property (nonatomic, retain) NSMutableArray* contactsList;
@property Person* invitedContact;

- (id)init;
+ (id)sharedInstance;
- (void)getAllContacts;
- (void)getAddressBookPermissions;
- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record;
- (NSNumber*)getMyPhoneNumber;

@end
