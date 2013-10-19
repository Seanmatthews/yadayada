//
//  ViewController.m
//  chattertest
//
//  Created by sean matthews on 10/17/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

@synthesize loginUserTextField;
@synthesize loginPassTextField;
@synthesize registerUserTextField;
@synthesize registerPassTextField;
@synthesize msgTextField;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    [self initConnection];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)initConnection
{
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"127.0.0.1", 80, &readStream, &writeStream);
    is = (__bridge NSInputStream*)readStream;
    os = (__bridge NSOutputStream*)writeStream;
    
    [is setDelegate:self];
    [os setDelegate:self];
    
    [is scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [os scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    
    [is open];
    [os open];
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
    NSLog(@"stream event %i",eventCode);
    if ([is hasBytesAvailable]) {
        NSLog(@"BYTESSSSSS");
    }
    else {
        NSLog(@"no bytes");
    }
    
    switch (eventCode) {
        case NSStreamEventOpenCompleted:
            NSLog(@"Stream opened");
            break;
            
        case NSStreamEventHasBytesAvailable:
            NSLog(@"bytes available");
            if (aStream == is) {
                uint8_t buffer[1024];
                int len;
                
                while ([is hasBytesAvailable]) {
                    len = [is read:buffer maxLength:1024];
                    NSLog(@"%i bytes",len);
                    if (len > 0) {
                        short msglen = ntohs(*(short*)buffer);
                        Byte msgType = buffer[2];
                        userId = CFSwapInt64(*(long long*)&buffer[3]);
                        NSLog(@"%i, %i, %lli",msglen,msgType,userId);
                        NSLog(@"%x %x %x %x %x %x %x %x",buffer[3],buffer[4],buffer[5],buffer[6],buffer[7],buffer[8],buffer[9],buffer[10]);
                        NSString *output = [[NSString alloc] initWithBytes:buffer length:len encoding:NSASCIIStringEncoding];
                        
                        if (nil != output) {
                            NSLog(@"server response: %@", output);
                        }
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

- (IBAction)registerButtonPressed:(id)sender
{
    NSString* textUsername = registerUserTextField.text;
    NSString* textPassword = registerPassTextField.text;
    NSLog(@"%@ : %@",textUsername,textPassword);
    
    short userLenBytes = [textUsername lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    short passLenBytes = [textPassword lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    short msgLengthBytes = userLenBytes+passLenBytes;
    unsigned char msgType = 1;

    short msgLen = htons(msgLengthBytes);
    short userLen = htons(userLenBytes);
    short passLen = htons(passLenBytes);
    unsigned char* user = (unsigned char*)[textUsername UTF8String];
    unsigned char* pass = (unsigned char*)[textPassword UTF8String];
    
    [os write:(unsigned char*)&msgLen maxLength:sizeof(short)];
    [os write:&msgType maxLength:sizeof(unsigned char)];
    [os write:(unsigned char*)&userLen maxLength:sizeof(short)];
    [os write:user maxLength:userLenBytes];
    [os write:(unsigned char*)&passLen maxLength:sizeof(short)];
    [os write:pass maxLength:passLenBytes];
}

- (IBAction)loginButtonPressed:(id)sender
{
    NSString* textUsername = loginUserTextField.text;
    NSString* textPassword = loginPassTextField.text;
    NSLog(@"%@ : %@",textUsername,textPassword);
    
    short userLenBytes = [textUsername lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    short passLenBytes = [textPassword lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    short msgLengthBytes = userLenBytes+passLenBytes;
    unsigned char msgType = 11;
    
    short msgLen = htons(msgLengthBytes);
    short userLen = htons(userLenBytes);
    short passLen = htons(passLenBytes);
    unsigned char* user = (unsigned char*)[textUsername UTF8String];
    unsigned char* pass = (unsigned char*)[textPassword UTF8String];
    
    [os write:(unsigned char*)&msgLen maxLength:sizeof(short)];
    [os write:&msgType maxLength:sizeof(unsigned char)];
    [os write:(unsigned char*)&userLen maxLength:sizeof(short)];
    [os write:user maxLength:userLenBytes];
    [os write:(unsigned char*)&passLen maxLength:sizeof(short)];
    [os write:pass maxLength:passLenBytes];
}

- (IBAction)sendMessageButtonPressed:(id)sender
{
    NSString* text = msgTextField.text;
    NSLog(@"%@",text);
    
    
}


@end
