package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterMessage {
    private final String login;
    private final String password;
    private final String handle;

    public RegisterMessage(String login, String password, String handle) {
        this.login = login;
        this.password = password;
        this.handle = handle;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getHandle() {
        return handle;
    }
}
