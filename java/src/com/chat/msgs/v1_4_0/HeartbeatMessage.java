package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class HeartbeatMessage implements Message {
    private final long timestamp;
    private final long latitude;
    private final long longitude;

    public HeartbeatMessage(ReadBuffer stream) {
        this.timestamp = stream.readLong();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
    }

    public HeartbeatMessage(long timestamp, long latitude, long longitude) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
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
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Heartbeat.getValue());
        stream.writeLong(getTimestamp());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Heartbeat");
        builder.append(",Timestamp=").append(getTimestamp());
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        return builder.toString();        
    }
} 
