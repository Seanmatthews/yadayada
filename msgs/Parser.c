- (void)parseMessage:(uint8_t*)buffer withLength:(int)length withCallback:(ViewController)cb {
   int idx = 0;
   while(idx < length - 1) {
        short msglen = CFSwapInt16BigToHost(*(short*)&buffer[idx]);
        idx += 2;
        Byte msgType = buffer[idx];
        idx += 1;
        NSLog(@"msg len: %d, msg type: %d",msglen,msgType);

       switch(msgType) {
       #for $msg in $msgs
           case ${msg.name}:
           #for $field in $msg.fields
           #if $field.type == 'byte'
              $field.type $field.name = buffer[idx++];
           #end if
           #if $field.type == 'short'
              $field.type $field.name = ntohs(*(short*)(buffer+idx));
              idx += 2;
           #end if
           #if $field.type == 'int'
              $field.type $field.name = ntohl(*(int*)(buffer+idx));
              idx += 4;
           #end if
           #if $field.type == 'long'  
              $field.type $field.name = CFSwapInt64BigToHost(*(long long*)&buffer[idx]);
              idex += 8;
           #end if
           #if $field.type == 'String'
              short strLen = ntohs(*(short*)(buffer+idx));
              idx += 2;
              NSString* $field.name = [[NSString alloc] initWithBytes:(buffer+idx) length: strLen encoding:STRENC];          
              idx += strLen 
           #end if
       #end for

              [cb on${msg.name}:$msg.val #for $field in $msg.fields#${field.name}:$field.name #end for#]
              break;

       #end for
       }
   }
}
