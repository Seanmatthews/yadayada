package com.chat.util;

import com.chat.util.buffer.ReadBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwoByteLengthMessageCracker implements ByteCracker {
    @Override
    public ReadBuffer crack(ReadBuffer readBuffer) {
        if (readBuffer.remaining() < 2)
            return null;

        short length = readBuffer.readShort(readBuffer.position());

        if (readBuffer.remaining() < 2 + length)
            return null;

        ReadBuffer slice = readBuffer.slice();
        slice.limit(2 + length);
        return slice;
    }
}
