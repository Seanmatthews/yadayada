package com.chat.server.impl;

import com.chat.Connection;

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
    public String readString() throws IOException {
        return din.readUTF();

        //byte[] ascii = new byte[din.readShort()];
        //int ignored = din.read(ascii);
        //return new String(ascii);
    }

    @Override
    public long readLong() throws IOException {
        return din.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return din.readInt();
    }

    @Override
    public short readShort() throws IOException {
        return din.readShort();
    }

    @Override
    public byte readByte() throws IOException {
        return din.readByte();
    }

    @Override
    public byte[] read(int length) throws IOException {
        byte[] bytes = new byte[length];
        int ignored = din.read(bytes);
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

        //dout.writeShort(value.length());
        //dout.write(value.getBytes());
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
