package com.chat.impl;

import com.chat.ChatroomActivity;

import java.util.Timer;

/**
 * Created by sean on 6/25/14.
 */
public class LCMVChatroomActivity implements ChatroomActivity {

    private long creationTime;
    private long messageGroupStartTime;
    private long messageCount;
    private long currentGroupMessageCount;
    private long lastMessageTimestamp;
    private double instantaneousActivity;
    private double activity;
    private final long NEW_GROUP_INTERVAL;
    private double seedGroupMessageRate;
    // private long wordCount;

    public LCMVChatroomActivity() {
        NEW_GROUP_INTERVAL = 15;
        creationTime = System.currentTimeMillis() / 1000L;
        messageGroupStartTime = creationTime;
        activity = 0;
        instantaneousActivity = 0;
        seedGroupMessageRate = 1.;
    }

    // Add a new message and update the chatroom activity
    // Returns: current chatroom activity
    @Override
    public short newMessage(String message) {
        lastMessageTimestamp = System.currentTimeMillis() / 1000L;
        messageCount++;

        if (isNewGroup()) {
            // A new group is starting, update based on last group
            updateActivity();
            messageGroupStartTime = System.currentTimeMillis() / 1000L;
        }
        else {
            currentGroupMessageCount++;
        }

        return activity < 1. ? (short)(activity * 100.) : 100;
    }

    @Override
    public short getActivityPercentage() {
        return activity < 1. ? (short)(activity * 100.) : 100;
    }

    private void updateActivity() {
        // message rate over last group
        double messageRate = (double)currentGroupMessageCount / (double)messageGroupAge();

        // group rate versus seed rate
        instantaneousActivity = messageRate / seedGroupMessageRate;

        // set new seed rate to last group rate
        seedGroupMessageRate = messageRate;

        // take average of new activity measure and the average of the all old ones
        activity = (activity + instantaneousActivity) / 2.;
    }

    // Determine whether a sufficient time interval has passed to start a new group
    private boolean isNewGroup() {
        long currentTime = System.currentTimeMillis() / 1000L;
        if (currentTime - lastMessageTimestamp > NEW_GROUP_INTERVAL) {
            return true;
        }
        return false;
    }

    // Get total age of this chatroom (or at least as long as activity is being calculated)
    private long chatroomAge() {
        return System.currentTimeMillis() / 1000L - creationTime;
    }

    // Get the age of the current group of messages
    private long messageGroupAge() {
        return System.currentTimeMillis() / 1000L - messageGroupStartTime;
    }
}
