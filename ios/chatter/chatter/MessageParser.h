//
//  MessageParser.h
//  chatter
//
//  Created by sean matthews on 10/24/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MessageTemplate.h"

@interface MessageParser : NSObject <NSXMLParserDelegate>
{
    MessageTemplate* currentMessageT;
}

- (void)parseMessagesFromXMLFile:(NSString*)filepath;

@property NSMutableArray* messages;

@end
