package com.chat.util.buffer.impl;

import com.chat.util.buffer.ReadBuffer;

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
public class WrappedReadBuffer implements ReadBuffer {
    private WrappedReadBuffer slice;

    protected ByteBuffer buffer;

    public WrappedReadBuffer(ByteBuffer buffer) {
        wrap(buffer);
    }

    @Override
    public void wrap(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public ByteBuffer getRawBuffer() {
        return buffer;
    }

    @Override
    public void advance(int length) {
        position(position() + length);
    }

    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public void position(int newPosition) {
        buffer.position(newPosition);
    }

    @Override
    public int limit() {
        return buffer.limit();
    }

    @Override
    public void limit(int newLimit) {
        buffer.limit(newLimit);
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    @Override
    public void flip() {
        buffer.flip();
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    @Override
    public void compact() {
        buffer.compact();
    }

    @Override
    public ReadBuffer slice() {
        if (slice == null) {
            slice = new WrappedReadBuffer(null);
        }

        slice.wrap(buffer.slice());
        return slice;
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    @Override
    public short readShort() {
        return buffer.getShort();
    }

    @Override
    public int readInt() {
        return buffer.getInt();
    }

    @Override
    public long readLong() {
        return buffer.getLong();
    }

    @Override
    public String readString() {
        short length = readShort();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot encode UTF-8");
        }
    }

    @Override
    public byte readByte(int position) {
        return buffer.get(position);
    }

    @Override
    public short readShort(int position) {
        return buffer.getShort(position);
    }

    @Override
    public int readInt(int position) {
        return buffer.getInt(position);
    }

    @Override
    public long readLong(int position) {
        return buffer.getLong(position);
    }

    @Override
    public String readString(int position) {
        int originalPosition = buffer.position();
        buffer.position(position);
        String str = readString();
        buffer.position(originalPosition);
        return str;
    }
}
