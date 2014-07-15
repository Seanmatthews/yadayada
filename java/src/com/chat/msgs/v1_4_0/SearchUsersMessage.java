package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class SearchUsersMessage implements Message {
    private final String query;

    public SearchUsersMessage(ReadBuffer stream) {
        this.query = stream.readString();
    }

    public SearchUsersMessage(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.SearchUsers.getValue());
        stream.writeString(getQuery());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=SearchUsers");
        builder.append(",Query=").append(getQuery());
        return builder.toString();        
    }
} 
