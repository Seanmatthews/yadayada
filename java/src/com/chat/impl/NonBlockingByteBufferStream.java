package com.chat.impl;

import com.chat.BinaryStream;
import com.chat.msgs.Message;
import com.chat.select.ClientSocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class NonBlockingByteBufferStream implements BinaryStream {
    private final ClientSocket channel;
    private final ByteBuffer output;
    private final Queue<Message> queue;

    private ByteBuffer input;
    private int writeStartPosition;

    public NonBlockingByteBufferStream(ClientSocket socket) throws IOException {
        this.channel = socket;
        this.output = ByteBuffer.allocateDirect(1024);
        this.queue = new ConcurrentLinkedQueue<>();
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
    public boolean isStream() {
        return false;
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
        checkReadBounds(1);
        return input.get();
    }

    @Override
    public short readShort() throws IOException {
        checkReadBounds(2);
        return input.getShort();
    }

    @Override
    public int readInt() throws IOException {
        checkReadBounds(4);
        return input.getInt();
    }

    @Override
    public long readLong() throws IOException {
        checkReadBounds(8);
        return input.getLong();
    }

    @Override
    public String readString() throws IOException {
        checkReadBounds(2);
        short length = input.getShort();
        byte[] strBytes = new byte[length];

        checkReadBounds(length);
        input.get(strBytes);
        return new String(strBytes, "UTF-8");
    }

    @Override
    public void startWriting() throws IOException {
        checkWriteBounds(2);
        writeStartPosition = output.position();
        output.position(writeStartPosition + 2);
    }

    @Override
    public void startWriting(int msgLength) throws IOException {
        throw new IOException("Invalid I/O Operation. Cannot specify length when writing");
    }

    private void checkReadBounds(int length) throws IOException {
        if (input.remaining() < length)
            throw new IOException("Not enough room in the write buffer");
    }

    private void checkWriteBounds(int length) throws IOException {
        if (output.remaining() < length)
            throw new IOException("Not enough room in the write buffer");
    }

    @Override
    public void finishWriting() throws IOException {
        output.putShort(writeStartPosition, (short) (output.position() - writeStartPosition - 2));
    }

    @Override
    public void queueMessage(Message message) throws IOException {
        if (queue.offer(message)) {
            channel.enableWrite(true);
        }
        else {
            throw new IOException("Too many messages in the queue to send. Terminating.");
        }
    }

    public boolean writeMessages() throws IOException {
        boolean messagesInQueue = true;

        if (output.position() > 0) {
            // got some stuff in the buffer, send it out
            writeFromBuffer();
        }

        // clear out the queue
        while (output.position() == 0) {
            Message msg = queue.poll();

            if (msg == null)  {
                messagesInQueue = false;
                break;
            }

            // write to buffer
            msg.write(this);
            // now flush the buffer
            writeFromBuffer();
        }

        if (output.position() > 0 || messagesInQueue) {
            // still stuff to write in the buffer
            return false;
        }
        else {
            // all done
            channel.enableWrite(false);
            return true;
        }
    }

    public void writeFromBuffer() throws IOException {
        output.flip();
        channel.write(output);

        if (output.hasRemaining()) {
            // still stuff to write in the buffer
            output.compact();
        }
        else {
            // still stuff in the queue
            output.clear();
        }
    }

    @Override
    public void writeByte(int value) throws IOException {
        checkWriteBounds(1);
        output.put((byte)value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        checkWriteBounds(2);
        output.putShort((short)value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        checkWriteBounds(4);
        output.putInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        checkWriteBounds(8);
        output.putLong(value);
    }

    @Override
    public void writeString(String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");

        checkWriteBounds(2 + bytes.length);
        output.putShort((short)bytes.length);
        output.put(bytes);
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
