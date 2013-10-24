#ifndef MessageTypes_h
#define MessageTypes_h
const int MESSAGE_API = 1;

typedef enum {
   Register = 1,      
   Login = 11,      
   Connect = 16,      
   SubmitMessage = 21,      
   SearchChatrooms = 31,      
   JoinChatroom = 33,      
   LeaveChatroom = 34,      
   CreateChatroom = 35,      
   RegisterAccept = 2,      
   RegisterReject = 3,      
   LoginAccept = 12,      
   LoginReject = 13,      
   ConnectAccept = 17,      
   ConnectReject = 18,      
   Message = 22,      
   Chatroom = 32,      
   JoinChatroomReject = 36,      
   JoinedChatroom = 37,      
   LeftChatroom = 38,      
   CreateChatroomReject = 38,      
} MessageTypes;

#endif
