//
//  Contacts.m
//  chatter
//
//  Created by sean matthews on 2/16/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "Contacts.h"


@implementation Contacts


- (id)init
{
    self = [super init];
    if (self) {
        accessGranted = NO;
        NSString * plistPath = [[NSBundle mainBundle] pathForResource:@"DialingCodes" ofType:@"plist"];
        phonePrefixDict = [NSDictionary dictionaryWithContentsOfFile:plistPath];
        _contactsList = [[NSMutableArray alloc] init];
    }
    return self;
}

+ (id)sharedInstance
{
    static dispatch_once_t pred = 0;
    __strong static id _sharedObject = nil;
    dispatch_once(&pred, ^{
        _sharedObject = [[self alloc] init];
        // Additional initialization can go here
    });
    return _sharedObject;
}

- (void)getAddressBookPermissions
{
    CFErrorRef *error = nil;
    ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(NULL, error);
    ABAddressBookRequestAccessWithCompletion(addressBook, ^(bool granted, CFErrorRef error) {
        accessGranted = granted;
    });
}

- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record
{
    NSNumber* phoneNum = nil;
    NSLocale *locale = [NSLocale currentLocale];
    NSString* countryCode = [phonePrefixDict objectForKey:[[locale objectForKey:NSLocaleCountryCode] lowercaseString]];
    ABMultiValueRef multiPhones = ABRecordCopyValue(record, kABPersonPhoneProperty);
    
    // Get iphone number
    for (int i=0; i<ABMultiValueGetCount(multiPhones); ++i) {
        NSString* label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiPhones, i);
        if ([label isEqualToString:(NSString*)kABPersonPhoneIPhoneLabel]) {
            CFStringRef phoneNumberRef = ABMultiValueCopyValueAtIndex(multiPhones, 0);
            NSString* phone = (__bridge NSString *) phoneNumberRef;
            NSString* fullPhone = [NSString stringWithFormat:@"%@%@",countryCode,phone];
            
            // Remove parens and dashes
            NSCharacterSet *charactersToRemove = [[ NSCharacterSet alphanumericCharacterSet ] invertedSet ];
            NSString *trimmed = [[fullPhone componentsSeparatedByCharactersInSet:charactersToRemove ] componentsJoinedByString:@"" ];
            phoneNum = [NSNumber numberWithLongLong:[trimmed longLongValue]];
        }
    }
    return phoneNum;
}

- (void)getAllContacts
{
    if (accessGranted) {
        ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(NULL, NULL);
        ABRecordRef me = ABAddressBookGetPersonWithRecordID(addressBook, 1);
        NSArray *thePeople = (__bridge NSArray *)ABAddressBookCopyArrayOfAllPeople(addressBook);

        for (id record in thePeople) {
            
            ABRecordRef person = (__bridge ABRecordRef)record;
            if (person == me) {
                [self setMyPhoneNumber:[self iPhoneNumberForRecord:me]];
                continue;
            }
            
            NSString* fName = (__bridge NSString *)(ABRecordCopyValue(person, kABPersonFirstNameProperty));
            NSString* lName = (__bridge NSString *)(ABRecordCopyValue(person, kABPersonLastNameProperty));
            NSNumber* iPhone = [self iPhoneNumberForRecord:person];
            
            if (!fName && !lName) {
                continue;
            }
            else if (!fName) {
                fName = @"";
            }
            else if (!lName) {
                lName = @"";
            }
            
            NSDictionary* personDict = [[NSDictionary alloc] initWithObjectsAndKeys:fName,@"fName",lName,@"lName",iPhone,@"iPhone",nil];
            [_contactsList addObject:personDict];
        }
    }
}










@end
