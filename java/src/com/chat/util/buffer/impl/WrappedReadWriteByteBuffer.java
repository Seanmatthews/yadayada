package com.chat.util.buffer.impl;

import com.chat.util.buffer.ReadWriteBuffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrappedReadWriteByteBuffer extends WrappedReadBuffer implements ReadWriteBuffer {
    public WrappedReadWriteByteBuffer(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public void writeByte(int value) {
        buffer.put((byte)value);
    }

    @Override
    public void writeShort(int value) {
        buffer.putShort((short)value);
    }

    @Override
    public void writeInt(int value) {
        buffer.putInt(value);
    }

    @Override
    public void writeLong(long value) {
        buffer.putLong(value);
    }

    @Override
    public void writeString(String value) {
        try {
            byte[] bytes = value.getBytes("UTF-8");
            writeShort(bytes.length);
            buffer.put(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot encode UTF-8");
        }
    }

    @Override
    public void writeByte(int position, int value) {
        buffer.put(position, (byte)value);
    }

    @Override
    public void writeShort(int position, int value) {
        buffer.putShort(position, (short)value);
    }

    @Override
    public void writeInt(int position, int value) {
        buffer.putInt(position, value);
    }

    @Override
    public void writeLong(int position, long value) {
        buffer.putLong(position, value);
    }
}
