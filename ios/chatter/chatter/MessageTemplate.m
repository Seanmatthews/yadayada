//
//  MessageTemplate.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MessageTemplate.h"

@implementation MessageTemplate

@synthesize name;
@synthesize val;
@synthesize origin;

- (id)init
{
    self = [super init];
    fieldDicts = [[NSMutableArray alloc] init];
    return self;
}

- (void)addFieldWithName:(NSString*)newName andType:(NSString*)type
{
//    NSMutableDictionary* field = [[NSMutableDictionary alloc] init];
//    [field setObject:type forKey:newName];

    NSLog(@"%@, %@",newName,type);
    [fieldDicts addObject:[NSDictionary dictionaryWithObjectsAndKeys:type, @"type", newName, @"name", nil]];
}

- (int)numFields
{
    return fieldDicts.count;
}

- (NSString*)getFieldNameAtIndex:(int)index
{
    NSDictionary* field = [fieldDicts objectAtIndex:index];
    return [field valueForKey:@"name"];
}

- (NSString*)getFieldTypeAtIndx:(int)index
{
    NSDictionary* field = [fieldDicts objectAtIndex:index];
    return [field valueForKey:@"type"];
}

- (void)addField:(NSDictionary*)field
{
    [fieldDicts addObject:field];
}

- (NSString*)stringValue
{
    NSMutableString* s = [[NSMutableString alloc] initWithCapacity:30];
    [s appendFormat:@"<msg name=%@ val=%@ origin=%@>\n",name,val,origin];
    for (NSDictionary* field in fieldDicts) {
        [s appendFormat:@"  <field name=%@ type=%@ />\n",[field objectForKey:@"name"],[field objectForKey:@"type"]];
    }
    [s appendFormat:@"</msg>"];
    return s;
}

@end
