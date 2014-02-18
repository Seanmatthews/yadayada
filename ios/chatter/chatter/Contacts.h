//
//  Contacts.h
//  chatter
//
//  Created by sean matthews on 2/16/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AddressBook/AddressBook.h>

@interface Contacts : NSObject
{
    BOOL accessGranted;
    NSDictionary* phonePrefixDict;
}

- (id)init;
+ (id)sharedInstance;
- (void)getAddressBookPermissions;
- (NSNumber*)getMyPhoneNumber;
- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record;

@end
