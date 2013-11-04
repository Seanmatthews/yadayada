package com.chat;

import com.chat.msgs.Message;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BinaryStream {
    String getUUID();
    int getAPIVersion();
    void close();
    void sendMessage(Message message, boolean immediate) throws IOException;

    byte   readByte() throws IOException;
    short  readShort() throws IOException;
    int    readInt() throws IOException;
    long   readLong() throws IOException;
    String readString() throws IOException;

    void startWriting() throws IOException;
    void finishWriting() throws IOException;
    void writeByte(int value) throws IOException;
    void writeShort(int value) throws IOException;
    void writeInt(int value) throws IOException;
    void writeLong(long value) throws IOException;
    void writeString(String value) throws IOException;

    @Deprecated
    boolean isStream();
    @Deprecated
    void startReading() throws IOException;
    @Deprecated
    void finishReading() throws IOException;
    @Deprecated
    void startWriting(int msgLength) throws IOException;
}