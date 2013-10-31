package com.chat.impl;

import com.chat.msgs.ValidationError;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/31/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ByteBufferParserListener {
    void onParsed(ByteBuffer buffer) throws InterruptedException, ExecutionException, ValidationError, IOException;
}
