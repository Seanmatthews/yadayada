package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateChatroomMessage {
    private final long userId;
    private final String chatroomName;

    public CreateChatroomMessage(long userId, String chatroomName) {
        this.userId = userId;
        this.chatroomName = chatroomName;
    }

    public long getUserId() {
        return userId;
    }

    public String getChatroomName() {
        return chatroomName;
    }
}
