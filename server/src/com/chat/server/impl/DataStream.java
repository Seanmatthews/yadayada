package com.chat.server.impl;

import com.chat.BinaryStream;
import com.chat.MessageTypes;
import com.chat.Utilities;

import java.io.*;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataStream implements BinaryStream {
    private final Socket socket;
    private final DataInputStream din;
    private final DataOutputStream dout;

    private int readBytes;
    private int readMsgBytes;

    private int writtenBytes;
    private int writeMsgBytes;

    public DataStream(Socket socket) throws IOException {
        this.socket = socket;
        this.din = new DataInputStream(socket.getInputStream());
        this.dout = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void close() {
        try {
            din.close();
        } catch (IOException ignored) {
        }

        try {
            dout.close();
        } catch (IOException ignored) {
        }

        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void startReading() throws IOException {
        short messageBytes = readShort();

        if (messageBytes <= 0 || messageBytes > 1000)
            throw new IOException("Invalid incoming data. Bytes in message: " + messageBytes);

        readMsgBytes = messageBytes;
        readBytes = 0;
    }

    @Override
    public void finishReading() throws IOException {
        if (readBytes > readMsgBytes) {
            // wtf, this is a mal-formed message
            throw new IOException("Invalid incoming data. Expected=" + readMsgBytes + " Read=" + readBytes);
        }

        if (readBytes < readMsgBytes) {
            // clear it out
            read(readMsgBytes - readBytes);
        }
    }

    @Override
    public String readString() throws IOException {
        String str = din.readUTF();
        readBytes += Utilities.getStrLen(str);
        return str;
    }

    @Override
    public long readLong() throws IOException {
        long value = din.readLong();
        readBytes += 8;
        return value;
    }

    @Override
    public int readInt() throws IOException {
        int value = din.readInt();
        readBytes += 4;
        return value;
    }

    @Override
    public short readShort() throws IOException {
        short value = din.readShort();
        readBytes += 2;
        return value;
    }

    @Override
    public byte readByte() throws IOException {
        byte value = din.readByte();
        readBytes++;
        return value;
    }

    @Override
    public byte[] read(int length) throws IOException {
        byte[] bytes = new byte[length];
        int ignored = din.read(bytes);
        readBytes += length;
        return bytes;
    }

    @Override
    public void startWriting(int msgLength) throws IOException {
        writeShort(msgLength);

        writeMsgBytes = msgLength;
        writtenBytes = 0;
    }

    @Override
    public void finishWriting() throws IOException {
        if (writtenBytes > writeMsgBytes) {
            throw new IOException("Wrote too many bytes. Expected=" + writeMsgBytes + " Sent=" + writeMsgBytes);
        }

        if (writtenBytes < writeMsgBytes) {
            throw new IOException("Wrote too few bytes. Expected=" + writeMsgBytes + " Sent=" + writeMsgBytes);
        }
    }

    @Override
    public void writeByte(int value) throws IOException {
        dout.writeByte(value);

        writtenBytes++;
    }

    @Override
    public void writeShort(int value) throws IOException {
        dout.writeShort(value);

        writtenBytes += 2;
    }

    @Override
    public void writeInt(int value) throws IOException {
        dout.writeInt(value);

        writtenBytes += 4;
    }

    @Override
    public void writeLong(long value) throws IOException {
        dout.writeLong(value);

        writtenBytes += 8;
    }

    @Override
    public void writeString(String value) throws IOException {
        dout.writeUTF(value);

        writtenBytes += Utilities.getStrLen(value);
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
