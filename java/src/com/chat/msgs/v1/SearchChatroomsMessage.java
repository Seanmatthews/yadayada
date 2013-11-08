package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class SearchChatroomsMessage implements Message {
    private final long latitude;
    private final long longitude;

    public SearchChatroomsMessage(ReadBuffer stream) {
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
    }

    public SearchChatroomsMessage(long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        stream.advance(2);
   
        stream.writeByte(MessageTypes.SearchChatrooms.getValue());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }
} 
