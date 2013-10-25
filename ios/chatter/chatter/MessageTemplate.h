//
//  MessageTemplate.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MessageTemplate : NSObject
{
    
    
    // array of dictionaries
    NSMutableArray* fieldDicts;
}

- (void)addField:(NSDictionary*)field;
- (void)addFieldWithName:(NSString*)string andType:(NSString*)type;
- (int)numFields;
- (NSString*)getFieldNameAtIndex:(int)index;
- (NSString*)getFieldTypeAtIndx:(int)index;
- (NSString*)stringValue;

@property NSString* name;
@property NSString* val;
@property NSString* origin;

@end
