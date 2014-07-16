package com.chat;

import com.chat.ChatMessage;

/**
 * Created by sean on 6/25/14.
 */
public interface ChatroomActivity {
    public short newMessage(ChatMessage message);
    public short getActivityPercentage();
}
