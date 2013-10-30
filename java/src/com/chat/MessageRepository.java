package com.chat;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MessageRepository {
    Message create(Chatroom chatroom, User sender, String message);
}
