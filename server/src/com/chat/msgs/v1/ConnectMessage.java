package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectMessage {
    private final int apiVersion;
    private final String uuid;

    public ConnectMessage(int apiVersion, String uuid) {
        this.apiVersion = apiVersion;
        this.uuid = uuid;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public String getUuid() {
        return uuid;
    }
}
