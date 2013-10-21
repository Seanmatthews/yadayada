//
//  ViewController.m
//  chattertest
//
//  Created by sean matthews on 10/17/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "ViewController.h"
#import "MessageTypes.h"

typedef const uint8_t* BUFTYPE;
const NSStringEncoding STRENC = NSUTF8StringEncoding;

@interface ViewController ()

@end

@implementation ViewController

@synthesize loginUserTextField;
@synthesize loginPassTextField;
@synthesize registerUserTextField;
@synthesize registerPassTextField;
@synthesize registerHandTextField;
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
                        [self parseMessage:buffer];
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

- (void)parseMessage:(uint8_t*)buffer
{
    short msglen = ntohs(*(short*)buffer);
    Byte msgType = buffer[2];
    NSString *error;
    short strLen = 0;
    int idx = 3;
    long long loginUserId, msgId, chatId, chatOwnerId;
    NSString* chatOwnerHandle;
    NSString* username;
    NSString* message;
    NSString* chatName;
    NSString* handle;
    long long latitude, longitude, radius;
    
    NSLog(@"msg len: %d, msg type: %d",msglen,msgType);
    
    switch (msgType) {
        case REGISTER_ACCEPT:
            userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            NSLog(@"REGISTER_ACCEPT [%lli]",userId);
            break;
            
        case REGISTER_REJECT:
            // we should get a 2 byte length for each string field received
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            error = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            NSLog(@"REGISTER_REJECT [%@]",error);
            break;
            
        case LOGIN_ACCEPT:
            loginUserId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            NSLog(@"LOGIN_ACCEPT [%lli]",loginUserId);
            break;
            
        case LOGIN_REJECT:
            // we should get a 2 byte length for each string field received
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            // TODO: Is this char conversion OK?
            error = [NSString stringWithUTF8String:(char*)(buffer+idx)];
            NSLog(@"LOGIN_REJECT [%@]",error);
            break;
            
        case MESSAGE:
            msgId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            loginUserId = CFSwapInt64(*(long long*)&buffer[idx]);
            idx += 8;
            chatId = CFSwapInt64(*(long long*)&buffer[idx]);
            idx += 8;
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            username = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            message = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            NSLog(@"MESSAGE [%lli, %lli, %lli, %@, %@]",msgId,loginUserId,chatId,username,message);
            break;
            
        case CHATROOM:
            chatId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            chatOwnerId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            strLen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
            idx += 2;
            chatName = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
            strLen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
            idx += 2;
            chatOwnerHandle = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
            latitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            longitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            radius = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            NSLog(@"CHATROOM [%lli, %lli, %@, %@, %lli, %lli, %lli]", chatId,chatOwnerId,chatName,chatOwnerHandle,latitude,longitude,radius);
            break;
            
        case JOIN_CHATROOM_FAILURE:
            chatId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            strLen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
            idx += 2;
            chatName = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
            strLen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
            idx += 2;
            error = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            NSLog(@"JOIN_CHATROOM_FAILURE [%lli, %@, %@]",chatId,chatName,error);
            break;
            
        case JOINED_CHATROOM:
            chatId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            loginUserId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            strLen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
            idx += 2;
            handle = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            NSLog(@"JOINED_CHATROOM [%lli, %lli, %@]",chatId,loginUserId,handle);
            break;
            
        case LEFT_CHATROOM:
            chatId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            loginUserId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            NSLog(@"LEFT_CHATROOM [%lli, %lli]",chatId,loginUserId);
            break;
            
        default:
            NSLog(@"Unrecognized message from server");
    }
    
    //NSLog(@"%i, %i, %lli",msglen,msgType,userId);
}

- (IBAction)registerButtonPressed:(id)sender
{
    NSString* user = registerUserTextField.text;
    NSString* pass = registerPassTextField.text;
    NSString* hand = registerHandTextField.text;
    NSLog(@"%@ : %@",user,pass);
    
    [self registerUsername:user password:pass handle:hand];
    
//    short userLenBytes = [textUsername lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
//    short passLenBytes = [textPassword lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
//    short msgLengthBytes = userLenBytes+passLenBytes;
//    unsigned char msgType = 1;
//
//    short msgLen = htons(msgLengthBytes);
//    short userLen = htons(userLenBytes);
//    short passLen = htons(passLenBytes);
//    unsigned char* user = (unsigned char*)[textUsername UTF8String];
//    unsigned char* pass = (unsigned char*)[textPassword UTF8String];
//    
//    [os write:(BUFTYPE)&msgLen maxLength:sizeof(short)];
//    [os write:&msgType maxLength:sizeof(unsigned char)];
//    [os write:(BUFTYPE)&userLen maxLength:sizeof(short)];
//    [os write:user maxLength:userLenBytes];
//    [os write:(BUFTYPE)&passLen maxLength:sizeof(short)];
//    [os write:pass maxLength:passLenBytes];
}

- (IBAction)loginButtonPressed:(id)sender
{
    NSString* user = loginUserTextField.text;
    NSString* pass = loginPassTextField.text;
    NSLog(@"%@ : %@",user,pass);
    
    [self loginWithUsername:user password:pass];
    
    //short userLenBytes = [textUsername lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    //short passLenBytes = [textPassword lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    //short msgLengthBytes = userLenBytes+passLenBytes;
    //unsigned char msgType = 11;
    
    //short msgLen = htons(msgLengthBytes);
    //short userLen = htons(userLenBytes);
    //short passLen = htons(passLenBytes);
    //unsigned char* user = (unsigned char*)[textUsername UTF8String];
    //unsigned char* pass = (unsigned char*)[textPassword UTF8String];
    
    //[os write:(BUFTYPE)&msgLen maxLength:sizeof(short)];
    //[os write:&msgType maxLength:sizeof(unsigned char)];
    //[os write:(BUFTYPE)&userLen maxLength:sizeof(short)];
    //[os write:user maxLength:userLenBytes];
    //[os write:(BUFTYPE)&passLen maxLength:sizeof(short)];
    //[os write:pass maxLength:passLenBytes];
}

- (IBAction)sendMessageButtonPressed:(id)sender
{
    NSString* text = msgTextField.text;
    NSLog(@"%@",text);
    
    
}

- (IBAction)createChatroomButtonPressed:(id)sender
{
    
}


#pragma mark - message to server functions

- (void)createChatroomWithName:(NSString*)name radius:(long long)chatRadius
{
    if (currentChatroomId < 0) {
        short msgLen = [name lengthOfBytesUsingEncoding:NSUTF8StringEncoding] + 33;
        [self writeShort:msgLen];
        [self writeByte:CREATE_CHATROOM];
        [self writeLong:userId];
        [self writeString:name];
        [self writeLong:currentLat];
        [self writeLong:currentLong];
        [self writeLong:chatRadius];
    }
}

- (void)searchChatrooms
{
    [self writeMessageHeaderWithSize:17 ofType:CREATE_CHATROOM];
    [self writeLong:currentLat];
    [self writeLong:currentLong];
}

- (void)joinChatroomWithId:(long long)chatId
{
    [self writeMessageHeaderWithSize:17 ofType:JOIN_CHATROOM];
    [self writeLong:userId];
    [self writeLong:chatId];
    [self writeLong:currentLat];
    [self writeLong:currentLong];
}

- (void)leaveChatroomWithId:(long long)chatId
{
    [self writeMessageHeaderWithSize:17 ofType:LEAVE_CHATROOM];
    [self writeLong:userId];
    [self writeLong:chatId];
}

- (void)registerHandle:(NSString*)hand
{
    int msgLen = [hand lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
    [self writeMessageHeaderWithSize:msgLen ofType:QUICK_REGISTER];
    [self writeString:hand];
}

- (void)registerUsername:(NSString*)user password:(NSString*)pass handle:(NSString*)hand
{
    int msgLen = [user lengthOfBytesUsingEncoding:NSUTF8StringEncoding] +
                 [pass lengthOfBytesUsingEncoding:NSUTF8StringEncoding] +
                 [hand lengthOfBytesUsingEncoding:NSUTF8StringEncoding] + 1;
    [self writeMessageHeaderWithSize:msgLen ofType:REGISTER];
    [self writeString:user];
    [self writeString:pass];
    [self writeString:hand];
}

- (void)loginWithUsername:(NSString*)user password:(NSString*)pass
{
    int msgLen = [user lengthOfBytesUsingEncoding:NSUTF8StringEncoding] +
                 [pass lengthOfBytesUsingEncoding:NSUTF8StringEncoding] + 1;
    [self writeMessageHeaderWithSize:msgLen ofType:LOGIN];
    [self writeString:user];
    [self writeString:pass];
}


#pragma mark - utility functions

- (void)writeString:(NSString*)string
{
    short len = CFSwapInt16HostToBig([string lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
    unsigned char* cString = (unsigned char*)[string UTF8String];
    [os write:(BUFTYPE)&len maxLength:sizeof(short)];
    [os write:cString maxLength:len];
}

- (void)writeMessageHeaderWithSize:(short)size ofType:(MessageTypes)type
{
    [self writeShort:size];
    [self writeByte:type];
    //short s = CFSwapInt16HostToBig(size);
    //[os write:(BUFTYPE)&s maxLength:sizeof(short)];
    //[os write:(BUFTYPE)type maxLength:1];
}
               
- (void)writeByte:(unsigned char)aByte
{
    [os write:(BUFTYPE)&aByte maxLength:1];
}

- (void)writeShort:(short)aShort
{
    short s = CFSwapInt16HostToBig(aShort);
    [os write:(BUFTYPE)&s maxLength:2];
}

- (void)writeLong:(long long)aLong
{
    long long l = CFSwapInt64HostToBig(aLong);
    [os write:(BUFTYPE)&l maxLength:8];
}


@end
