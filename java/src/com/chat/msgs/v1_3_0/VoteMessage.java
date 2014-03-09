package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class VoteMessage implements Message {
    private final long voterId;
    private final long votedId;
    private final long msgId;
    private final long chatroomId;
    private final byte upvote;

    public VoteMessage(ReadBuffer stream) {
        this.voterId = stream.readLong();
        this.votedId = stream.readLong();
        this.msgId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.upvote = stream.readByte();
    }

    public VoteMessage(long voterId, long votedId, long msgId, long chatroomId, byte upvote) {
        this.voterId = voterId;
        this.votedId = votedId;
        this.msgId = msgId;
        this.chatroomId = chatroomId;
        this.upvote = upvote;
    }

    public long getVoterId() {
        return voterId;
    }

    public long getVotedId() {
        return votedId;
    }

    public long getMsgId() {
        return msgId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public byte getUpvote() {
        return upvote;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Vote.getValue());
        stream.writeLong(getVoterId());
        stream.writeLong(getVotedId());
        stream.writeLong(getMsgId());
        stream.writeLong(getChatroomId());
        stream.writeByte(getUpvote());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Vote");
        builder.append(",VoterId=").append(getVoterId());
        builder.append(",VotedId=").append(getVotedId());
        builder.append(",MsgId=").append(getMsgId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",Upvote=").append(getUpvote());
        return builder.toString();        
    }
} 
