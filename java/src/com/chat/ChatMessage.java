package com.chat;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatMessage {
    private final long id;
    private final Chatroom chatroom;
    private final User sender;
    private final String message;
    private final long timestamp;

    public ChatMessage(long id, Chatroom chatroom, User sender, String message, long timestamp) {
        this.id = id;
        this.chatroom = chatroom;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.getId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return chatroom + " " + sender + ": " + message;
    }
}
