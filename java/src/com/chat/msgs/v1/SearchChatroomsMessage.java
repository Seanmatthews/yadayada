package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class SearchChatroomsMessage implements Message {
    private final long latitude;
    private final long longitude;

    public SearchChatroomsMessage(BinaryStream stream) throws IOException {
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
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           SearchChatroomsMessage msg = this;
           stream.startWriting(1 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.SearchChatrooms.getValue());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.finishWriting();
    }
} 
