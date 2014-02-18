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
    
    //    __block BOOL accessGranted = NO;
    if (ABAddressBookRequestAccessWithCompletion != NULL) { // we're on iOS 6
        dispatch_semaphore_t sema = dispatch_semaphore_create(0);
        ABAddressBookRequestAccessWithCompletion(addressBook, ^(bool granted, CFErrorRef error) {
            accessGranted = granted;
            dispatch_semaphore_signal(sema);
        });
        dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER);
        
    }
    else { // we're on iOS 5 or older
        accessGranted = YES;
    }
}

- (NSNumber*)getMyPhoneNumber
{
    NSNumber* phoneNum = nil;
    if (accessGranted) {
        
        CFErrorRef *error = nil;
        NSLocale *locale = [NSLocale currentLocale];
        NSString* countryCode = [phonePrefixDict objectForKey:[[locale objectForKey:NSLocaleCountryCode] lowercaseString]];
        ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(NULL, error);
        ABRecordRef me = ABAddressBookGetPersonWithRecordID(addressBook, 1);
        ABMultiValueRef multiPhones = ABRecordCopyValue(me, kABPersonPhoneProperty);
        
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
    }
    return phoneNum;
}

- (NSNumber*)iPhoneNumberForRecord:(ABRecordRef)record
{
    NSNumber* phoneNum = nil;
    CFErrorRef *error = nil;
    NSLocale *locale = [NSLocale currentLocale];
    NSString* countryCode = [phonePrefixDict objectForKey:[[locale objectForKey:NSLocaleCountryCode] lowercaseString]];
    ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(NULL, error);
    ABRecordRef me = ABAddressBookGetPersonWithRecordID(addressBook, 1);
    ABMultiValueRef multiPhones = ABRecordCopyValue(me, kABPersonPhoneProperty);
    
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


@end
