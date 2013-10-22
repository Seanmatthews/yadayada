package com.chat.server.impl;

import com.chat.Connection;
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
public class SocketConnection implements Connection {
    private final Socket socket;
    private final DataInputStream din;
    private final DataOutputStream dout;

    private int readBytes;
    private int msgBytes;

    public SocketConnection(Socket socket) throws IOException {
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
    public void startReadingMessage(int messageBytes) throws IOException {
        if (messageBytes <= 0 || messageBytes > 1000)
            throw new IOException("Invalid incoming data. Bytes in message: " + messageBytes);

        msgBytes = messageBytes;
        readBytes = 0;
    }

    @Override
    public void finishReadingMessage() throws IOException {
        if (readBytes > msgBytes) {
            // wtf, this is a mal-formed message
            throw new IOException("Invalid incoming data. Expected=" + msgBytes + " Read=" + readBytes);
        }

        if (readBytes < msgBytes) {
            // clear it out
            read(msgBytes - readBytes);
        }
    }

    @Override
    public String readString() throws IOException {
        String str = din.readUTF();
        readBytes += Utilities.getStringLength(str);
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
    public void writeByte(int value) throws IOException {
        dout.writeByte(value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        dout.writeShort(value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        dout.writeInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        dout.writeLong(value);
    }

    @Override
    public void writeString(String value) throws IOException {
        dout.writeUTF(value);
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
