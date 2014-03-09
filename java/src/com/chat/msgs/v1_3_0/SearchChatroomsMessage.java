package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class SearchChatroomsMessage implements Message {
    private final long latitude;
    private final long longitude;
    private final byte onlyJoinable;
    private final long metersFromCoords;

    public SearchChatroomsMessage(ReadBuffer stream) {
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
        this.onlyJoinable = stream.readByte();
        this.metersFromCoords = stream.readLong();
    }

    public SearchChatroomsMessage(long latitude, long longitude, byte onlyJoinable, long metersFromCoords) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.onlyJoinable = onlyJoinable;
        this.metersFromCoords = metersFromCoords;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public byte getOnlyJoinable() {
        return onlyJoinable;
    }

    public long getMetersFromCoords() {
        return metersFromCoords;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.SearchChatrooms.getValue());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeByte(getOnlyJoinable());
        stream.writeLong(getMetersFromCoords());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=SearchChatrooms");
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        builder.append(",OnlyJoinable=").append(getOnlyJoinable());
        builder.append(",MetersFromCoords=").append(getMetersFromCoords());
        return builder.toString();        
    }
} 
