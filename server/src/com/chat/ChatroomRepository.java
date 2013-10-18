package com.chat;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatroomRepository {
    Chatroom createChatroom(User owner, String name);
    List<Chatroom> search(ChatroomSearchCriteria search);
    Chatroom get(long chatroomId);
}
