package com.chat.msgs.v1;

public class RegisterMessage {
    private final String userName;
    private final String password;
    private final String handle;

    public RegisterMessage(String userName, String password, String handle) {
        this.userName = userName;
        this.password = password;
        this.handle = handle;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getHandle() {
        return handle;
    }
} 
