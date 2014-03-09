package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class InviteUserSuccessMessage implements Message {
    private final long inviteeUserId;
    private final String inviteeHandle;
    private final String chatroomName;

    public InviteUserSuccessMessage(ReadBuffer stream) {
        this.inviteeUserId = stream.readLong();
        this.inviteeHandle = stream.readString();
        this.chatroomName = stream.readString();
    }

    public InviteUserSuccessMessage(long inviteeUserId, String inviteeHandle, String chatroomName) {
        this.inviteeUserId = inviteeUserId;
        this.inviteeHandle = inviteeHandle;
        this.chatroomName = chatroomName;
    }

    public long getInviteeUserId() {
        return inviteeUserId;
    }

    public String getInviteeHandle() {
        return inviteeHandle;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.InviteUserSuccess.getValue());
        stream.writeLong(getInviteeUserId());
        stream.writeString(getInviteeHandle());
        stream.writeString(getChatroomName());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=InviteUserSuccess");
        builder.append(",InviteeUserId=").append(getInviteeUserId());
        builder.append(",InviteeHandle=").append(getInviteeHandle());
        builder.append(",ChatroomName=").append(getChatroomName());
        return builder.toString();        
    }
} 
