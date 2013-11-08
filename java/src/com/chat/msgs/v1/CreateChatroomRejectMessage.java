package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class CreateChatroomRejectMessage implements Message {
    private final String chatroomName;
    private final String reason;

    public CreateChatroomRejectMessage(ReadBuffer stream) {
        this.chatroomName = stream.readString();
        this.reason = stream.readString();
    }

    public CreateChatroomRejectMessage(String chatroomName, String reason) {
        this.chatroomName = chatroomName;
        this.reason = reason;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.CreateChatroomReject.getValue());
        stream.writeString(getChatroomName());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=CreateChatroomReject");
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
