package com.chat.msgs;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 8:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidationError extends Exception {
    private final String message;

    public ValidationError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
