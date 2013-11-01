package com.chat.impl;

import com.chat.BinaryStream;
import com.chat.msgs.Message;
import com.chat.msgs.ValidationError;
import com.chat.select.ClientSocket;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class NonBlockingByteBufferStream implements BinaryStream {
    private final ClientSocket socket;
    private final ByteBuffer rawInput;
    private final ByteBuffer output;
    private final Queue<Message> queue;

    private ByteBuffer input;
    private int writeStartPosition;
    private int APIVersion;
    private String UUID;

    public NonBlockingByteBufferStream(ClientSocket socket) throws IOException {
        this.socket = socket;
        this.output = ByteBuffer.allocateDirect(1024);
        this.rawInput = ByteBuffer.allocateDirect(1024);
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean isStream() {
        return false;
    }

    public void read(ByteBufferParserListener listener) throws IOException, InterruptedException, ExecutionException, ValidationError {
        int read = socket.read(rawInput);

        if (read == -1) {
            throw new EOFException("End of Stream");
        }

        rawInput.flip();

        while ((input = parse()) != null) {
            int nextMessage = rawInput.position() + input.remaining() + 2;
            listener.onParsed(input);
            rawInput.position( nextMessage );
        }

        if (rawInput.hasRemaining()) {
            rawInput.compact();
        }
        else {
            rawInput.clear();
        }
    }

    public ByteBuffer parse() {
        // do we have enough bytes for message size?
        if (rawInput.remaining() < 2)
            return null;

        // get message size
        short length = rawInput.getShort(rawInput.position());

        // do we have enough bytes remaining in the message?
        if (rawInput.remaining() < 2 + length)
            return null;

        // Slice for the message - skip message size
        ByteBuffer slice = rawInput.slice();
        slice.position(slice.position() + 2);
        slice.limit(slice.position() + length);

        return slice;
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
    public void sendMessage(Message message, boolean immediate) throws IOException {
        if (immediate && output.position() == 0) {
            // write to buffer
            message.write(this);
            // now flush the buffer
            writeFromBuffer();

            if (output.position() > 0) {
                socket.enableWrite(true);
            }
        }
        else {
            if (queue.offer(message)) {
                socket.enableWrite(true);
            }
            else {
                throw new IOException("Too many messages in the queue to send. Terminating.");
            }
        }
    }

    public void writeMessages() throws IOException {
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
            return;
        }

        // all done
        socket.enableWrite(false);
    }

    public void writeFromBuffer() throws IOException {
        output.flip();
        socket.write(output);

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
        output.put((byte) value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        checkWriteBounds(2);
        output.putShort((short) value);
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
        output.putShort((short) bytes.length);
        output.put(bytes);
    }

    @Override
    public String toString() {
        return socket.toString();
    }

    public void setAPIVersion(int APIVersion) {
        this.APIVersion = APIVersion;
    }

    @Override
    public int getAPIVersion() {
        return APIVersion;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public String getUUID() {
        return UUID;
    }
}
