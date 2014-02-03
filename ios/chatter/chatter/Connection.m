//
//  Connection.m
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Connection.h"

@implementation Connection

const int IMAGE_SERVER_PORT = 5001;
const CGFloat JPEG_COMPRESSION_QUALITY = 0.75;

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

- (int)getImageServerPort
{
    return IMAGE_SERVER_PORT;
}

- (void)connect
{
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"ec2-54-198-228-23.compute-1.amazonaws.com", 5000, &readStream, &writeStream);
    //CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"192.168.1.108", 5001, &readStream, &writeStream);
    is = (__bridge NSInputStream*)readStream;
    os = (__bridge NSOutputStream*)writeStream;
    
    [is setDelegate:self];
    [os setDelegate:self];
    
    [is scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [os scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [is open];
    [os open];
}

- (void)connectToImageServer
{
    CFReadStreamRef imgReadStream;
    CFWriteStreamRef imgWriteStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"ec2-54-200-202-37.us-west-2.compute.amazonaws.com", 5001, &imgReadStream, &imgWriteStream);
    
    imgIs = (__bridge NSInputStream*)imgReadStream;
    imgOs = (__bridge NSOutputStream*)imgWriteStream;
    
    [imgIs setDelegate:self];
    [imgOs setDelegate:self];
    
    [imgIs scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [imgOs scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [imgIs open];
    [imgOs open];
}

- (void)sendMessage:(MessageBase*)message
{
    //NSLog(@"Sending message of type %d",(MessageTypes)message.type);
    NSData* d = [MessageUtils serializeMessage:message];
    [os write:[d bytes] maxLength:[d length]];
}

- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url
{
    NSMutableData* imageData = [[NSMutableData alloc] init];
    long long uid = CFSwapInt64HostToBig(userId);
    NSData* b = [NSData dataWithBytes:&uid length:sizeof(long long)];
    [imageData appendData:b];
    [imageData appendData:UIImageJPEGRepresentation(image, JPEG_COMPRESSION_QUALITY)];
    [imgOs write:[b bytes] maxLength:[b length]];
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
    NSDictionary* tmpControllers = [[NSDictionary alloc] initWithDictionary:controllers copyItems:YES];
    memcpy(internalBuffer+internalBufferLen, buffer, length);
    internalBufferLen += length;

    while (internalBufferLen > 1) {
        short msgLen = CFSwapInt16BigToHost(*(short*)&internalBuffer[0]);
        if (internalBufferLen-2 < msgLen) {
            break;
        }
        
        MessageBase* m = [MessageUtils deserializeMessage:internalBuffer+2 withLength:msgLen];
        for (NSString* sender in tmpControllers) {
            ((void (^)(MessageBase*))[tmpControllers objectForKey:sender])(m);
        }

        // move unused bytes to the beginning of the buffer
        internalBufferLen -= (msgLen+2);
        if (internalBufferLen > 0) {
            memcpy(internalBuffer, internalBuffer+msgLen+2, internalBufferLen);
        }
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
    
    switch (eventCode) {
        case NSStreamEventOpenCompleted:
            NSLog(@"Stream opened");
            _streamReady = YES;
            break;
            
        case NSStreamEventHasBytesAvailable:
            if (aStream == is) {
                BUFDECLTYPE buffer[1024];
                int len;
                
                while ([is hasBytesAvailable]) {
                    len = [is read:buffer maxLength:1024];
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
            NSLog(@"Event End Encountered");
            break;
            
        case NSStreamEventHasSpaceAvailable:
            break;
            
        case NSStreamEventNone:
            NSLog(@"Event None");
            break;
            
        default:
            NSLog(@"unknown event");
    }
}

@end
