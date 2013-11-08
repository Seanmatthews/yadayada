package com.chat.select;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EventHandler {
    void onAccept();
    void onConnect();
    void onRead();
    void onWrite();
}
