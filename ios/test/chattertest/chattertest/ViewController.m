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
const NSStringEncoding STRENC = NSASCIIStringEncoding;

@interface ViewController ()

@end

@implementation ViewController

@synthesize loginUserTextField;
@synthesize loginPassTextField;
@synthesize registerUserTextField;
@synthesize registerPassTextField;
@synthesize registerHandTextField;
@synthesize msgTextField;
@synthesize chatIdTextField;
@synthesize chatNameTextField;
@synthesize chatRadiusTextField;
@synthesize currentChatIdTextField;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    loginUserTextField.returnKeyType = UIReturnKeyDone;
    loginPassTextField.returnKeyType = UIReturnKeyDone;
    registerUserTextField.returnKeyType = UIReturnKeyDone;
    registerPassTextField.returnKeyType = UIReturnKeyDone;
    registerHandTextField.returnKeyType = UIReturnKeyDone;
    msgTextField.returnKeyType = UIReturnKeyDone;
    chatIdTextField.returnKeyType = UIReturnKeyDone;
    chatNameTextField.returnKeyType = UIReturnKeyDone;
    chatRadiusTextField.returnKeyType = UIReturnKeyDone;
    currentChatIdTextField.returnKeyType = UIReturnKeyDone;
    
    [loginUserTextField setDelegate:self];
    [loginPassTextField setDelegate:self];
    [registerUserTextField setDelegate:self];
    [registerPassTextField setDelegate:self];
    [registerHandTextField setDelegate:self];
    [msgTextField setDelegate:self];
    [chatIdTextField setDelegate:self];
    [chatNameTextField setDelegate:self];
    [chatRadiusTextField setDelegate:self];
    [currentChatIdTextField setDelegate:self];
    
    [self initConnection];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
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
    //NSLog(@"stream event %i",eventCode);
    
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

- (void)parseMessage:(uint8_t*)buffer withLength:(int)length
{
    
    NSString *error;
    short strLen = 0;
    int idx = 0;
    long long loginUserId, msgId, chatId, chatOwnerId;
    NSString* chatOwnerHandle;
    NSString* username;
    NSString* message;
    NSString* chatName;
    NSString* handle;
    long long latitude, longitude, radius;
    
    
    
    while (idx < length-1) {
        short msglen = ntohs(*(short*)&buffer[idx]);
        idx += 2;
        Byte msgType = buffer[idx];
        idx += 1;
        NSLog(@"msg len: %d, msg type: %d",msglen,msgType);
        
    switch (msgType) {
        case REGISTER_ACCEPT:
            userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            NSLog(@"REGISTER_ACCEPT [%lli]",userId);
            break;
            
        case REGISTER_REJECT:
            // we should get a 2 byte length for each string field received
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            error = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
            NSLog(@"REGISTER_REJECT [%@]",error);
            break;
            
        case LOGIN_ACCEPT:
            //TODO: userId should be persistent and not taken from here
            userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            idx += 8;
            NSLog(@"LOGIN_ACCEPT [%lli]",userId);
            break;
            
        case LOGIN_REJECT:
            // we should get a 2 byte length for each string field received
            strLen = ntohs(*(short*)(buffer+idx));
            idx += 2;
            error = [[NSString alloc] initWithBytes:(buffer+idx) length:strLen encoding:STRENC];
            idx += strLen;
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
            idx += strLen;
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
            //latitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            //idx += 8;
            //longitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            //idx += 8;
            //radius = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
            //idx += 8;
            NSLog(@"CHATROOM [%lli, %lli, %@, %@]", chatId,chatOwnerId,chatName,chatOwnerHandle);//,latitude,longitude,radius);
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
            idx += strLen;
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
            idx += strLen;
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
    }
    
    //NSLog(@"%i, %i, %lli",msglen,msgType,userId);
}

#pragma mark - UI element actions

- (IBAction)registerButtonPressed:(id)sender
{
    NSString* user = registerUserTextField.text;
    NSString* pass = registerPassTextField.text;
    NSString* hand = registerHandTextField.text;
    NSLog(@"%@ : %@",user,pass);
    
    [self registerUsername:user password:pass handle:hand];
}

- (IBAction)loginButtonPressed:(id)sender
{
    NSString* user = loginUserTextField.text;
    NSString* pass = loginPassTextField.text;
    
    [self loginWithUsername:user password:pass];
}

- (IBAction)sendMessageButtonPressed:(id)sender
{
    NSString* text = msgTextField.text;
    long long chatId = [currentChatIdTextField.text longLongValue];
    [self sendMessage:text toChat:chatId];
    
}

- (IBAction)joinChatButtonPressed:(id)sender
{
    long long chatId = [chatIdTextField.text longLongValue];
    [self joinChatroomWithId:chatId];
    
}

- (IBAction)leaveChatButtonPressed:(id)sender
{
    long long chatId = [chatIdTextField.text longLongValue];
    [self leaveChatroomWithId:chatId];
}

- (IBAction)createChatButtonPressed:(id)sender
{
    long long radius = [chatRadiusTextField.text longLongValue];
    NSString* name = chatNameTextField.text;
    [self createChatroomWithName:name radius:radius];
}

- (IBAction)searchChatsButtonPressed:(id)sender
{
    [self searchChatrooms];
}


#pragma mark - message to server functions

- (void)createChatroomWithName:(NSString*)name radius:(long long)chatRadius
{
    short msgLen = [name lengthOfBytesUsingEncoding:STRENC] + 35;
    [self writeShort:msgLen];
    [self writeByte:CREATE_CHATROOM];
    [self writeLong:userId];
    [self writeString:name];
    [self writeLong:currentLat];
    [self writeLong:currentLong];
    [self writeLong:chatRadius];
}

- (void)searchChatrooms
{
    [self writeMessageHeaderWithSize:17 ofType:SEARCH_CHATROOMS];
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
    int msgLen = [hand lengthOfBytesUsingEncoding:STRENC];
    [self writeMessageHeaderWithSize:msgLen ofType:QUICK_REGISTER];
    [self writeString:hand];
}

- (void)registerUsername:(NSString*)user password:(NSString*)pass handle:(NSString*)hand
{
    int msgLen = [user lengthOfBytesUsingEncoding:STRENC] +
                 [pass lengthOfBytesUsingEncoding:STRENC] +
                 [hand lengthOfBytesUsingEncoding:STRENC] + 7;
    [self writeMessageHeaderWithSize:msgLen ofType:REGISTER];
    [self writeString:user];
    [self writeString:pass];
    [self writeString:hand];
}

- (void)loginWithUsername:(NSString*)user password:(NSString*)pass
{
    int msgLen = [user lengthOfBytesUsingEncoding:STRENC] +
                 [pass lengthOfBytesUsingEncoding:STRENC] + 5;
    [self writeMessageHeaderWithSize:msgLen ofType:LOGIN];
    [self writeString:user];
    [self writeString:pass];
}

- (void)sendMessage:(NSString*)message toChat:(long long)chatId
{
    int msgLen = [message lengthOfBytesUsingEncoding:STRENC] +
                 [userHandle lengthOfBytesUsingEncoding:STRENC] + 33;
    [self writeMessageHeaderWithSize:msgLen ofType:SUBMIT_MESSAGE];
    [self writeLong:userId];
    [self writeLong:chatId];
    [self writeString:message];
}


#pragma mark - utility functions

- (void)writeString:(NSString*)string
{
    //short len = CFSwapInt16HostToBig();
    //len = [string lengthOfBytesUsingEncoding:STRENC];
    //NSLog(@"writeString len: %d",[string lengthOfBytesUsingEncoding:STRENC]);
    //NSLog(@"%s",(const uint8_t*)[string UTF8String]);
    short len = (short)[string lengthOfBytesUsingEncoding:STRENC];
    [self writeShort:len];
    [os write:(BUFTYPE)[string cStringUsingEncoding:STRENC] maxLength:len];
}

- (void)writeMessageHeaderWithSize:(short)size ofType:(MessageTypes)type
{
    [self writeShort:size];
    [self writeByte:type];
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
