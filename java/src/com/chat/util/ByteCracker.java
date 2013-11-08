package com.chat.util;

import com.chat.util.buffer.ReadBuffer;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ByteCracker {
    ReadBuffer crack(ReadBuffer readBuffer);
}
