//
//  Contacts.m
//  chatter
//
//  Created by sean matthews on 2/16/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

#import "Contacts.h"

@implementation Person

- (id)initWithFirstName:(NSString*)fName lastName:(NSString*)lName phoneNumber:(NSNumber*)phoneNum
{
    if (self = [super init]) {
        dict = [[NSDictionary alloc] initWithObjectsAndKeys:fName,@"fName",lName,@"lName",phoneNum,@"iPhone",nil];
    }
    return self;
}


- (NSString*)getFirstName
{
    return [dict objectForKey:@"fName"];
}

- (NSString*)getLastName
{
    return [dict objectForKey:@"lName"];
}

- (NSNumber*)getPhoneNumber
{
    return [dict objectForKey:@"iPhone"];
}

@end

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
    ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, error);
    
    if (ABAddressBookGetAuthorizationStatus() == kABAuthorizationStatusNotDetermined) {
        ABAddressBookRequestAccessWithCompletion(addressBookRef, ^(bool granted, CFErrorRef error) {
            accessGranted = granted;
        });
    }
    else if (ABAddressBookGetAuthorizationStatus() == kABAuthorizationStatusAuthorized) {
        // The user has previously given access, add the contact
        accessGranted = YES;
    }
    else {
        accessGranted = NO;
    }
    NSLog(@"%d",accessGranted);
}

- (NSNumber*)getMyPhoneNumber
{
    NSNumber* phoneNum = nil;
    NSLocale *locale = [NSLocale currentLocale];
    NSString* countryCode = [phonePrefixDict objectForKey:[[locale objectForKey:NSLocaleCountryCode] lowercaseString]];
    ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, NULL);
    ABRecordRef me = ABAddressBookGetPersonWithRecordID(addressBookRef, 1);
    ABMultiValueRef multiPhones = ABRecordCopyValue(me, kABPersonPhoneProperty);
    
    // Get iphone number
    for (int i=0; i<ABMultiValueGetCount(multiPhones); ++i) {
        NSString* label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiPhones, i);
        //        if ([label isEqualToString:(NSString*)kABPersonPhoneIPhoneLabel]) {
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

- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record
{
    NSNumber* phoneNum = nil;
    NSLocale *locale = [NSLocale currentLocale];
    NSString* countryCode = [phonePrefixDict objectForKey:[[locale objectForKey:NSLocaleCountryCode] lowercaseString]];
    ABMultiValueRef multiPhones = ABRecordCopyValue(record, kABPersonPhoneProperty);
    
    // Get iphone number
    for (int i=0; i<ABMultiValueGetCount(multiPhones); ++i) {
        NSString* label = (__bridge NSString*)ABMultiValueCopyLabelAtIndex(multiPhones, i);
//        if ([label isEqualToString:(NSString*)kABPersonPhoneIPhoneLabel]) {
        if ([label isEqualToString:(NSString*)kABPersonPhoneMobileLabel]) {
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

//        [self setMyPhoneNumber:[self iPhoneNumberForRecord:me]];
        
        for (id record in thePeople) {
            
            ABRecordRef person = (__bridge ABRecordRef)record;
            if (person == me) {
                
                continue;
            }
            
            NSString* fName = (__bridge NSString *)(ABRecordCopyValue(person, kABPersonFirstNameProperty));
            NSString* lName = (__bridge NSString *)(ABRecordCopyValue(person, kABPersonLastNameProperty));
            NSNumber* iPhone = [self iPhoneNumberForRecord:person];
            
            if (!fName && !lName) {
                continue;
            }
            else if (!iPhone) {
                continue;
            }
            else if (!fName) {
                fName = @"";
            }
            else if (!lName) {
                lName = @"";
            }
            
            Person* personDict = [[Person alloc] initWithFirstName:fName lastName:lName phoneNumber:iPhone];
            [_contactsList addObject:personDict];
        }
    }
}










@end
