package com.chat;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Connection {
    void close();

    String readString() throws IOException;
    long readLong() throws IOException;
    int readInt() throws IOException;
    short readShort() throws IOException;
    byte readByte() throws IOException;
    byte[] read(int length) throws IOException;

    void writeByte(int value) throws IOException;
    void writeShort(int value) throws IOException;
    void writeInt(int value) throws IOException;
    void writeLong(long value) throws IOException;
    void writeString(String value) throws IOException;


}
