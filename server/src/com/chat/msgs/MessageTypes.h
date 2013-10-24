#ifndef MessageTypes_h
#define MessageTypes_h
const int MESSAGE_API = $apiVersion;

typedef enum {
#for $msg in $msgs
   $msg.name = $msg.val,      
#end for
} MessageTypes;

#endif
