package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class UserInfoMessage implements Message {
    private final long userId;
    private final String handle;
    private final String uuid;

    public UserInfoMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.handle = stream.readString();
        this.uuid = stream.readString();
    }

    public UserInfoMessage(long userId, String handle, String uuid) {
        this.userId = userId;
        this.handle = handle;
        this.uuid = uuid;
    }

    public long getUserId() {
        return userId;
    }

    public String getHandle() {
        return handle;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.UserInfo.getValue());
        stream.writeLong(getUserId());
        stream.writeString(getHandle());
        stream.writeString(getUuid());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=UserInfo");
        builder.append(",UserId=").append(getUserId());
        builder.append(",Handle=").append(getHandle());
        builder.append(",Uuid=").append(getUuid());
        return builder.toString();        
    }
} 
