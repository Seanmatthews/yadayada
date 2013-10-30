package com.chat.impl;

import com.chat.BinaryStream;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    private ByteBuffer input;
    private final ByteBuffer output;

    private int writeStartPosition;

    public ByteBufferStream(SocketChannel socket) throws IOException {
        channel = socket;
        output = ByteBuffer.allocateDirect(10 * 1024);
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

    private int readSync(int bytesToRead) throws IOException {
        input.clear();
        input.limit(bytesToRead);

        int bytesRead = 0;
        while (bytesRead < bytesToRead) {
            int read = channel.read(input);

            if (read == -1)
                throw new EOFException("End of stream");

            bytesRead += read;
        }

        input.flip();
        return bytesRead;
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
        writeStartPosition = output.position();
        output.position(writeStartPosition + 2);
    }

    @Override
    public void finishWriting() throws IOException {
        output.putShort(writeStartPosition, (short) (output.position() - writeStartPosition - 2));
        output.flip();

        while(output.hasRemaining())
            channel.write(output);

        output.clear();
    }

    @Override
    public void writeByte(int value) throws IOException {
        output.put((byte)value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        output.putShort((short)value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        output.putInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        output.putLong(value);
    }

    @Override
    public void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        output.putShort((short)bytes.length);
        output.put(bytes);
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
