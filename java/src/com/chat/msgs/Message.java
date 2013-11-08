package com.chat.msgs;

import com.chat.util.buffer.ReadWriteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Message {
    void write(ReadWriteBuffer stream);
}
