package com.chat.msgs.v1;

public class LoginMessage {
    private final String userName;
    private final String password;

    public LoginMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
} 
