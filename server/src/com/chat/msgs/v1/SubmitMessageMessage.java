package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmitMessageMessage {
    private final long userId;
    private final long chatroomId;
    private final String msg;

    public SubmitMessageMessage(long userId, long chatroomId, String msg) {
        //To change body of created methods use File | Settings | File Templates.
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.msg = msg;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getMsg() {
        return msg;
    }
}
