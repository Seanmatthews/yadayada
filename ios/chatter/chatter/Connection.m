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
        _streamReady = NO;
        controllers = [[NSMutableDictionary alloc] init];
        internalBufferLen = 0;
    }
    return self;
}

- (void)connect
{
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"ec2-54-200-202-37.us-west-2.compute.amazonaws.com", 5000, &readStream, &writeStream);
    is = (__bridge NSInputStream*)readStream;
    os = (__bridge NSOutputStream*)writeStream;
    
    [is setDelegate:self];
    [os setDelegate:self];
    
    [is scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [os scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [is open];
    [os open];
}

- (void)sendMessage:(MessageBase*)message
{
    NSLog(@"Sending message of type %d",(MessageTypes)message.type);
    NSData* d = [MessageUtils serializeMessage:message];
    [os write:[d bytes] maxLength:[d length]];
}

// See https://developer.apple.com/library/ios/documentation/cocoa/conceptual/ProgrammingWithObjectiveC/WorkingwithBlocks/WorkingwithBlocks.html
// to understand how this callback function works.
//- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length
//{
//    NSLog(@"parse");
//    if (length < 3) {
//        memcpy(internalBuffer, buffer+internalBufferLen, length);
//        internalBufferLen = length;
//    }
//    
//    if ((internalBufferLen > 2 && length != internalBufferLen) || length > 2) {
//        int len = internalBufferLen+length;
//        BUFDECLTYPE tmp[len];
//        memcpy(tmp, internalBuffer, internalBufferLen);
//        memcpy(tmp+internalBufferLen, buffer, length);
//        internalBufferLen = 0;
//        while (len > 0) {
//            MessageBase* m = [MessageUtils deserializeMessage:tmp withLength:&len];
//            for (NSString* sender in controllers) {
//                ((void (^)(MessageBase*))[controllers objectForKey:sender])(m);
//            }
//        }
//    }
//}

- (void)parseMessage:(BUFTYPE)buffer withLength:(int)length
{
    NSLog(@"parse");
    memcpy(internalBuffer+internalBufferLen, buffer, length);
    internalBufferLen += length;
    NSLog(@"%d bytes total",internalBufferLen);
    
//    int msgLen = CFSwapInt16BigToHost(*(short*)&internalBuffer[0]);
//    while (msgLen <= internalBufferLen-2 && internalBufferLen > 1) {
    while (internalBufferLen > 1) {
        short msgLen = CFSwapInt16BigToHost(*(short*)&internalBuffer[0]);
        NSLog(@"msg len: %d",msgLen);
        if (internalBufferLen-2 < msgLen) {
            break;
        }
        
        MessageBase* m = [MessageUtils deserializeMessage:internalBuffer+2 withLength:msgLen];
        for (NSString* sender in controllers) {
            ((void (^)(MessageBase*))[controllers objectForKey:sender])(m);
        }
        
        // move unused bytes to the beginning of the buffer
        internalBufferLen -= (msgLen+2);
        memcpy(internalBuffer, internalBuffer+msgLen+2, internalBufferLen);
//        if (internalBufferLen > 1) {
//            msgLen = CFSwapInt16BigToHost(*(short*)&internalBuffer[0]);
//        }
    }
}


#pragma mark - Adding/Removing Callbacks

- (void)addCallbackBlock:(void (^)(MessageBase*))block fromSender:(NSString*)sender
{
    [controllers setObject:block forKey:sender];
}

- (void)removeCallbackBlockFromSender:(NSString*)sender
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
            _streamReady = YES;
            break;
            
        case NSStreamEventHasBytesAvailable:
            NSLog(@"bytes available");
            if (aStream == is) {
                BUFDECLTYPE buffer[1024];
                int len;
                
                while ([is hasBytesAvailable]) {
                    len = [is read:buffer maxLength:1024];
                    NSLog(@"%i bytes",len);
                    if (len > 0) {
                        [self parseMessage:buffer withLength:len];
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
