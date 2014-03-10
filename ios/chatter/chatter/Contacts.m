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


@interface Contacts()
@property (nonatomic) BOOL haveContactsAccess;
@end

@implementation Contacts




- (id)init
{
    self = [super init];
    if (self) {
        _haveContactsAccess = NO;
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
    ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(nil, nil);
    
//    __block BOOL accessGranted = NO;
    
    if (ABAddressBookRequestAccessWithCompletion != NULL) { // we're on iOS 6
//        dispatch_semaphore_t sema = dispatch_semaphore_create(0);
        
        ABAddressBookRequestAccessWithCompletion(addressBook, ^(bool granted, CFErrorRef error) {
//            accessGranted = granted;
//            dispatch_semaphore_signal(sema);
            [self setHaveContactsAccess:granted];
        });
        
//        dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER);
    }
//    NSLog(@"%d",accessGranted);
//    _haveContactsAccess = accessGranted;
}

- (NSNumber*)getMyPhoneNumber
{
    NSNumber* phoneNum = nil;
    if (_haveContactsAccess) {
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
            
            NSCharacterSet *charactersToRemove = [[ NSCharacterSet alphanumericCharacterSet ] invertedSet ];
            
            NSString* phone = (__bridge NSString *) phoneNumberRef;
            NSString* phoneTrimmed = [[phone componentsSeparatedByCharactersInSet:charactersToRemove] componentsJoinedByString:@""];
            
            // Only add country code if listed phone number is below the standard 1-215-555-1212.
            // This is a kludge, but I don't know a better way.
            NSString* fullPhone;
            if ([phoneTrimmed length] < 11) {
                fullPhone = [NSString stringWithFormat:@"%@%@",countryCode,phone];
            }
            else {
                fullPhone = phoneTrimmed;
            }
            
            NSString *trimmed = [[fullPhone componentsSeparatedByCharactersInSet:charactersToRemove ] componentsJoinedByString:@"" ];
            phoneNum = [NSNumber numberWithLongLong:[trimmed longLongValue]];
        }
    }
    return phoneNum;
}

- (void)getAllContacts
{
    if (_haveContactsAccess) {
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
