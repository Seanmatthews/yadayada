package com.chat.util.buffer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ReadWriteBuffer extends ReadBuffer {
    void writeByte(int value);
    void writeShort(int value);
    void writeInt(int value);
    void writeLong(long value);
    void writeString(String value);

    void writeByte(int position, int value);
    void writeShort(int position, int value);
    void writeInt(int position, int value);
    void writeLong(int position, long value);
}
