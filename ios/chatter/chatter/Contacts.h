//
//  Contacts.h
//  chatter
//
//  Created by sean matthews on 2/16/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AddressBook/AddressBook.h>
#import "Messages.h"


@interface Contacts : NSObject
{
    BOOL accessGranted;
//    NSMutableArray* contactsList;
    NSDictionary* phonePrefixDict;
}

@property (atomic, retain) NSNumber* myPhoneNumber;
@property (nonatomic, retain) NSMutableArray* contactsList;

- (id)init;
+ (id)sharedInstance;
- (void)getAllContacts;
- (void)getAddressBookPermissions;
- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record;

@end
