//
//  Connection.m
//  chatter
//
//  Created by sean matthews on 10/28/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "Connection.h"
#import "UserDetails.h"
#import "Messages.h"
#import "Location.h"

@interface Connection()

- (void)parseMessage:(BUFTYPE)buffer withLength:(NSInteger)length;
- (void)sendHeartbeatMessage;

@end

@implementation Connection
{
    NSInputStream* is;
    NSOutputStream* os;
//    NSInputStream* imgIs;
//    NSOutputStream* imgOs;
    BUFDECLTYPE internalBuffer[8096];
    int internalBufferLen;
    __block BOOL reconnecting;
    
    dispatch_queue_t sendMessageQueue;
    dispatch_queue_t inputStreamQueue;
    
    NSTimer* heartbeatTimer;
}


const int IMAGE_SERVER_PORT = 5001;
const CGFloat JPEG_COMPRESSION_QUALITY = 0.75;

- (id)init
{
    self = [super init];
    if (self) {
        reconnecting = NO;
        internalBufferLen = 0;
        inputStreamQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0);
        sendMessageQueue = dispatch_queue_create("outgoingMessageQueue", DISPATCH_QUEUE_SERIAL);
        _connectMode = @"";
        heartbeatTimer = nil;
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

// Whenever the heartbeat interval is set, stop previously
// scheduled heartbeats and start a new one.
- (void)setHeartbeatInterval:(NSTimeInterval)heartbeatInterval
{
    _heartbeatInterval = heartbeatInterval;
    [heartbeatTimer invalidate];
    heartbeatTimer = [NSTimer scheduledTimerWithTimeInterval:heartbeatInterval
                                                      target:self
                                                    selector:@selector(sendHeartbeatMessage)
                                                    userInfo:nil
                                                     repeats:YES];
}

- (void)connect
{
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    NSString* server;
    
#if DEBUG
    server = @"ec2-54-198-228-23.compute-1.amazonaws.com";
#else
    server = @"ec2-184-73-141-125.compute-1.amazonaws.com";
#endif
    
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)server, 5000, &readStream, &writeStream);
    
    // Local testing -- change IP for you
    //CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"192.168.1.108", 5001, &readStream, &writeStream);
    
    NSLog(@"connect mode %@",_connectMode);
    
//    CFReadStreamSetProperty(readStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanFalse);
//    CFWriteStreamSetProperty(writeStream, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanFalse);
//    CFReadStreamSetProperty(readStream, kCFStreamNetworkServiceType, kCFStreamNetworkServiceTypeVoIP);
//    CFWriteStreamSetProperty(writeStream, kCFStreamNetworkServiceType, kCFStreamNetworkServiceTypeVoIP);
    
    is = (__bridge NSInputStream*)readStream;
    os = (__bridge NSOutputStream*)writeStream;
    
    [is setDelegate:self];
    [os setDelegate:self];
    
    [is scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [os scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [is open];
    [os open];
}

- (void)reconnect
{
    NSLog(@"in reconnect, (os, is) status: %lu, %lu", (unsigned long)[os streamStatus],(unsigned long)[is streamStatus]);
    if ([is streamStatus] == NSStreamStatusNotOpen ||
        [is streamStatus] == NSStreamStatusClosed ||
        [is streamStatus] == NSStreamStatusError) {
        if (!reconnecting) {
            reconnecting = YES;
            [self connect];
        }
    }
}

// WARNING: Be careful with the messages sent into this method--
// once an object is passed, altering and resubmitting that object
// to this function will result in the block not seeing the changes
// to the object.
- (void)sendMessage:(MessageBase*)message
{
    dispatch_async(sendMessageQueue, ^{
        NSData* d = [MessageUtils serializeMessage:message];
        [os write:[d bytes] maxLength:[d length]];
    });
}

- (void)sendHeartbeatMessage
{
    HeartbeatMessage* hbm = [[HeartbeatMessage alloc] init];
    hbm.timestamp = [NSDate timeIntervalSinceReferenceDate];
    hbm.latitude = [[Location sharedInstance] currentLat];
    hbm.longitude = [[Location sharedInstance] currentLong];
    [self sendMessage:hbm];
}

- (void)parsePushNotification:(NSDictionary*)notification
{
    NSLog(@"notification %@",notification);
    MessageBase* m = [MessageUtils messageWithPushNotification:notification];
    [[NSNotificationQueue defaultQueue] enqueueNotification:[NSNotification
                                                             notificationWithName:NSStringFromClass([m class])
                                                             object:m]
                                               postingStyle:NSPostNow];
}

- (void)parseMessage:(BUFTYPE)buffer withLength:(NSInteger)length
{
//    NSDictionary* tmpControllers = [[NSDictionary alloc] initWithDictionary:controllers copyItems:YES];
    memcpy(internalBuffer+internalBufferLen, buffer, length);
    internalBufferLen += length;

    while (internalBufferLen > 1) {
        short msgLen = CFSwapInt16BigToHost(*(short*)&internalBuffer[0]);
        if (internalBufferLen-2 < msgLen) {
            break;
        }
        
        MessageBase* m = [MessageUtils deserializeMessage:internalBuffer+2 withLength:msgLen];
        
        // Post message as a notification to all observers for that message
        if (m) {
            [[NSNotificationQueue defaultQueue] enqueueNotification:[NSNotification
                                                                     notificationWithName:NSStringFromClass([m class])
                                                                     object:m]
                                                       postingStyle:NSPostNow];
        }
        
        // move unused bytes to the beginning of the buffer
        internalBufferLen -= (msgLen+2);
        if (internalBufferLen > 0) {
            memcpy(internalBuffer, internalBuffer+msgLen+2, internalBufferLen);
        }
    }
}

- (void)streamReset
{
    // Need to reset the recorded stream on the server
    StreamResetMessage* srm = [[StreamResetMessage alloc] init];
    srm.userId = [[UserDetails sharedInstance] userId];
    srm.appAwake = [[UIApplication sharedApplication] applicationState] == UIApplicationStateActive ? 1 : 0;
    [self sendMessage:srm];
}


#pragma mark - NSStreamDelegate implementation

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
//    dispatch_async(inputStreamQueue, ^{
        switch (eventCode) {
            case NSStreamEventOpenCompleted:
                NSLog(@"Stream opened");
                if ([is streamStatus] == NSStreamStatusOpen &&
                    [os streamStatus] == NSStreamStatusOpen &&
                    reconnecting) {
                    
                    NSLog(@"reconnecting");
                    [self streamReset];
                    
                    // Send notification to rejoin all chatrooms
//                    [[NSNotificationQueue defaultQueue] enqueueNotification:[NSNotification
//                                                                             notificationWithName:@"RejoinChatroomsNotification"
//                                                                             object:nil]
//                                                               postingStyle:NSPostNow];
                    reconnecting = NO;
                }
                break;
                
            case NSStreamEventHasBytesAvailable:
                if (aStream == is) {
                    BUFDECLTYPE buffer[1024];
                    NSInteger len;
                    
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
                [self reconnect];
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
//    });
}


//- (void)connectToImageServer
//{
//    CFReadStreamRef imgReadStream;
//    CFWriteStreamRef imgWriteStream;
//    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"ec2-54-200-202-37.us-west-2.compute.amazonaws.com", 5001, &imgReadStream, &imgWriteStream);
//
//    imgIs = (__bridge NSInputStream*)imgReadStream;
//    imgOs = (__bridge NSOutputStream*)imgWriteStream;
//
//    [imgIs setDelegate:self];
//    [imgOs setDelegate:self];
//
//    [imgIs scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
//    [imgOs scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
//
//    [imgIs open];
//    [imgOs open];
//}

//- (void)uploadImage:(UIImage*)image forUserId:(long long)userId toURL:(NSString*)url
//{
//    NSMutableData* imageData = [[NSMutableData alloc] init];
//    long long uid = CFSwapInt64HostToBig(userId);
//    NSData* b = [NSData dataWithBytes:&uid length:sizeof(long long)];
//    [imageData appendData:b];
//    [imageData appendData:UIImageJPEGRepresentation(image, JPEG_COMPRESSION_QUALITY)];
//    [imgOs write:[b bytes] maxLength:[b length]];
//}

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


@end
