package com.chat;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserRepository {
    User registerUser(String login, String password, String handle);
    User quickRegisterUser(String handle);
    User login(String login, String password);
    User get(long id);
    void addUser(User user);
}
