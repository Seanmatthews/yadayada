package com.chat.msgs.v1;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/22/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginMessage {
    private final String login;
    private final String password;

    public LoginMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
