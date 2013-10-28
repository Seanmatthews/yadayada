//
//  Connection.m
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Connection.h"

@implementation Connection

- (id)init
{
    self = [super init];
    if (self) {
        controllers = [[NSMutableDictionary alloc] init];
        
        CFReadStreamRef readStream;
        CFWriteStreamRef writeStream;
        CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"127.0.0.1", 5000, &readStream, &writeStream);
        is = (__bridge NSInputStream*)readStream;
        os = (__bridge NSOutputStream*)writeStream;
        
        [is setDelegate:self];
        [os setDelegate:self];
        
        [is scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        [os scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        
        [is open];
        [os open];
    }
    return self;
}

- (void)sendMessage:(MessageBase*)message
{
    NSData* d = [MessageUtils serializeMessage:message];
    [os write:[d bytes] maxLength:[d length]];
}

// See https://developer.apple.com/library/ios/documentation/cocoa/conceptual/ProgrammingWithObjectiveC/WorkingwithBlocks/WorkingwithBlocks.html
// to understand how this callback function works.
- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length
{
    while (length > 0) {
        MessageBase* m = [MessageUtils deserializeMessage:buffer withLength:&length];
        for (id sender in controllers) {
            ((void (^)(MessageBase*))[controllers objectForKey:sender])(m);
        }
    }
}


#pragma mark - Adding/Removing Callbacks

- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(id)sender
{
    [controllers setObject:block forKey:sender];
}

- (void)removeCallbackBlockFromSender:(id)sender
{
    [controllers removeObjectForKey:sender];
}


#pragma mark - NSStreamDelegate implementation

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
    //NSLog(@"stream event %i",eventCode);
    
    switch (eventCode) {
        case NSStreamEventOpenCompleted:
            NSLog(@"Stream opened");
            break;
            
        case NSStreamEventHasBytesAvailable:
            NSLog(@"bytes available");
            if (aStream == is) {
                BUFTYPE buffer[1024];
                int len;
                
                while ([is hasBytesAvailable]) {
                    len = [is read:buffer maxLength:1024];
                    NSLog(@"%i bytes",len);
                    if (len > 0) {
                        [self parseMessage:buffer withLength:len];
                        //NSLog(@"LEN: %d",len);
                    }
                }
            }
            break;
            
        case NSStreamEventErrorOccurred:
            NSLog(@"Cannot connect to the host");
            break;
            
        case NSStreamEventEndEncountered:
            break;
            
        case NSStreamEventHasSpaceAvailable:
            break;
            
        case NSStreamEventNone:
            break;
            
        default:
            NSLog(@"unknown event");
    }
}

@end
