//
//  MessageParser.m
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "MessageParser.h"

@implementation MessageParser

@synthesize messages;

- (id)init
{
    self = [super init];
    messages = [[NSMutableArray alloc] init];
    return self;
}

#pragma mark - XML message schema parsing

- (void)parseMessagesFromXMLFile:(NSString*)filepath
{
    //NSString *filepath = [[NSBundle mainBundle] pathForResource:@"v1" ofType:@"xml"];
    NSXMLParser* parser = [[NSXMLParser alloc] initWithData:[NSData dataWithContentsOfFile:filepath]];
    [parser setDelegate:self];
    [parser parse];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    if ([elementName isEqualToString:@"msg"]) {
        //alloc some object to parse value into
        currentMessageT = [[MessageTemplate alloc] init];
        //NSLog(@"%@, %@, %@",[attributeDict objectForKey:@"name"],[attributeDict objectForKey:@"val"],[attributeDict objectForKey:@"origin"]);
        currentMessageT.name = [attributeDict objectForKey:@"name"];
        currentMessageT.val = [attributeDict objectForKey:@"val"];
        currentMessageT.origin = [attributeDict objectForKey:@"origin"];
    }
    else if ([elementName isEqualToString:@"field"]) {
        [currentMessageT addFieldWithName:[attributeDict objectForKey:@"name"] andType:[attributeDict objectForKey:@"type"]];
    }
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    // We don't have any XML values
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    if ([elementName isEqualToString:@"msg"]) {
        [messages addObject:currentMessageT];
    }
}

@end
