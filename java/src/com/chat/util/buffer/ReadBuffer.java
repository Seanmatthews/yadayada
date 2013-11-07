package com.chat.util.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ReadBuffer {
    void wrap(ByteBuffer buffer);
    ByteBuffer getRawBuffer();

    void advance(int length);

    int position();
    void position(int newPosition);

    int limit();
    void limit(int newLimit);

    int remaining();
    boolean hasRemaining();

    void flip();
    void clear();
    void compact();

    ReadBuffer slice();

    byte   readByte();
    short  readShort();
    int    readInt();
    long   readLong();
    String readString();

    byte   readByte(int position);
    short  readShort(int position);
    int    readInt(int position);
    long   readLong(int position);
    String readString(int position);
}
