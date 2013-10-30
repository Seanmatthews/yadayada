package com.chat.impl;

import com.chat.BinaryStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteBufferStream implements BinaryStream {
    private final SocketChannel channel;
    private final Selector selector;
    private final ByteBuffer output;

    private ByteBuffer input;
    private int writeStartPosition;

    public ByteBufferStream(SocketChannel channel, Selector selector) throws IOException {
        this.channel = channel;
        this.output = ByteBuffer.allocateDirect(1024);
        this.selector = selector;
    }

    public void onReadAvailable(ByteBuffer slice) {
        input = slice;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void startReading() throws IOException {
        // nothing, already read
    }

    @Override
    public void finishReading() throws IOException {
        // nothing, already read
    }

    @Override
    public byte readByte() throws IOException {
        return input.get();
    }

    @Override
    public short readShort() throws IOException {
        return input.getShort();
    }

    @Override
    public int readInt() throws IOException {
        return input.getInt();
    }

    @Override
    public long readLong() throws IOException {
        return input.getLong();
    }

    @Override
    public String readString() throws IOException {
        short length = input.getShort();
        byte[] strBytes = new byte[length];
        input.get(strBytes);
        return new String(strBytes, "UTF-8");
    }

    @Override
    public void startWriting(int msgLength) throws IOException {
        checkBounds(2);
        writeStartPosition = output.position();
        output.position(writeStartPosition + 2);
    }

    private void checkBounds(int length) throws IOException {
        if (output.remaining() < length)
            throw new IOException("Not enough room in the write buffer");
    }

    @Override
    public void finishWriting() throws IOException {
        output.putShort(writeStartPosition, (short) (output.position() - writeStartPosition - 2));

        write();
    }

    public void write() throws IOException {
        if ( output.position() == 0 ) {
            channel.register(selector, SelectionKey.OP_READ);
            output.clear();
            return;
        }

        output.flip();
        channel.write(output);

        if (output.hasRemaining()) {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            output.compact();
        }
        else {
            channel.register(selector, SelectionKey.OP_READ);
            output.clear();
        }
    }

    @Override
    public void writeByte(int value) throws IOException {
        checkBounds(1);
        output.put((byte)value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        checkBounds(2);
        output.putShort((short)value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        checkBounds(4);
        output.putInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        checkBounds(8);
        output.putLong(value);
    }

    @Override
    public void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");

        checkBounds(2 + bytes.length);
        output.putShort((short)bytes.length);
        output.put(bytes);
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
