- (void)parseMessage:(uint8_t*)buffer withLength:(int)length withCallback:(ViewController)cb {
   int idx = 0;
   while(idx < length - 1) {
        short msglen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
        idx += 2;
        Byte msgType = buffer[idx];
        idx += 1;
        NSLog(@"msg len: %d, msg type: %d",msglen,msgType);

       switch(msgType) {
           case RegisterAccept:
              long userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;

              [cb onRegisterAccept:2 userId:userId ]
              break;

           case RegisterReject:
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* reason = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onRegisterReject:3 reason:reason ]
              break;

           case LoginAccept:
              long userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;

              [cb onLoginAccept:12 userId:userId ]
              break;

           case LoginReject:
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* reason = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onLoginReject:13 reason:reason ]
              break;

           case ConnectAccept:
              int APIVersion = ntohl(*(int*)(buffer+idx));
              idx += 4;
              long globalChatId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;

              [cb onConnectAccept:17 APIVersion:APIVersion globalChatId:globalChatId ]
              break;

           case ConnectReject:
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* reason = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onConnectReject:18 reason:reason ]
              break;

           case Message:
              long messageId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long messageTimestamp = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long senderId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long chatroomId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* senderHandle = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* message = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onMessage:22 messageId:messageId messageTimestamp:messageTimestamp senderId:senderId chatroomId:chatroomId senderHandle:senderHandle message:message ]
              break;

           case Chatroom:
              long chatroomId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long chatroomOwnerId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* chatroomName = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* chatroomOwnerHandle = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 
              long latitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long longitude = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long radius = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;

              [cb onChatroom:32 chatroomId:chatroomId chatroomOwnerId:chatroomOwnerId chatroomName:chatroomName chatroomOwnerHandle:chatroomOwnerHandle latitude:latitude longitude:longitude radius:radius ]
              break;

           case JoinChatroomReject:
              long chatroomId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* reason = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onJoinChatroomReject:36 chatroomId:chatroomId reason:reason ]
              break;

           case JoinedChatroom:
              long chatroomId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* userHandle = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onJoinedChatroom:37 chatroomId:chatroomId userId:userId userHandle:userHandle ]
              break;

           case LeftChatroom:
              long chatroomId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
              long userId = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;

              [cb onLeftChatroom:38 chatroomId:chatroomId userId:userId ]
              break;

           case CreateChatroomReject:
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* chatroomName = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* reason = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 

              [cb onCreateChatroomReject:38 chatroomName:chatroomName reason:reason ]
              break;

       }
   }
}
