package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuickRegisterMessage {
    private final String handle;

    public QuickRegisterMessage(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }
}
