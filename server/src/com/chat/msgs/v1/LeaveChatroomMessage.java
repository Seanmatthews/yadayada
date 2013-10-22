package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class LeaveChatroomMessage {
    private final long userId;
    private final long chatroomId;

    public LeaveChatroomMessage(long userId, long chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }
}
