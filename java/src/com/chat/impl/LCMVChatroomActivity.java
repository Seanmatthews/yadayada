package com.chat.impl;

import com.chat.ChatMessage;
import com.chat.ChatroomActivity;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Timer;

/**
 * Created by sean on 6/25/14.
 *
 * This class implements the ChatroomActivity interface to provide a chatroom activity number, which is
 * representative of how active a particular chatroom is at any one instant.
 *
 * How it works:
 * The initial chat activity value starts at 0%.
 *
 */
public class LCMVChatroomActivity implements ChatroomActivity, Serializable {

    private long creationTime;
    private long messageGroupStartTime;
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
        activity = 0.99; // start at 100%
        instantaneousActivity = 0;
        seedGroupMessageRate = 1.; // 1 per second
        lastMessageTimestamp = creationTime; // faux first message
        currentGroupMessageCount = 0;
    }

    // Add a new message and update the chatroom activity
    // Returns: current chatroom activity as percentage
    @Override
    public short newMessage(ChatMessage message) {
        if (isNewGroup()) {
            // A new group is starting, update based on last group
            updateActivity();
            messageGroupStartTime = System.currentTimeMillis() / 1000L;
            seedGroupMessageRate = (double)currentGroupMessageCount / (double)messageGroupAge();
            currentGroupMessageCount = 0;
        }

        currentGroupMessageCount++;

        // group rate versus seed rate --
        // this will be the current group
        double messageRate = (double)currentGroupMessageCount / (double)messageGroupAge();
        instantaneousActivity = messageRate / seedGroupMessageRate;

        lastMessageTimestamp = System.currentTimeMillis() / 1000L;

        return getActivityPercentage();
    }

    // Return the current chatroom activity as percentage
    @Override
    public short getActivityPercentage() {
        return (short)((activity < 1. ? activity : 1.) * 100.);
    }

    // Update chat activity
    private void updateActivity() {
        // take average of new activity measure and the average of the all old ones
        // Algorithm:
        // (0.5 * prior m/s + 0.5 * current m/s) * 100
        // where current m/s = num messages this group / group duration / last group message rate
        // where prior m/s = last getActivityPercentage()
        double priorActivity = activity < 1. ? activity : 1.;
        double currentActivity = instantaneousActivity < 1. ? instantaneousActivity : 1.;
        activity = (priorActivity + currentActivity) / 2.;
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

        out.writeLong(creationTime);
        out.writeLong(messageGroupStartTime);
        out.writeLong(currentGroupMessageCount);
        out.writeLong(lastMessageTimestamp);
        out.writeDouble(instantaneousActivity);
        out.writeDouble(activity);
        out.writeDouble(seedGroupMessageRate);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();

        creationTime = new Long(in.readLong());
        messageGroupStartTime = new Long(in.readLong());
        currentGroupMessageCount = new Long(in.readLong());
        lastMessageTimestamp = new Long(in.readLong());
        instantaneousActivity = new Double(in.readDouble());
        activity = new Double(in.readDouble());
        seedGroupMessageRate = new Double(in.readDouble());
    }
}

