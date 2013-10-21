package com.chat;

import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserRepository {
    Future<User> registerUser(String login, String password, String handle, UserCompletionHandler completionHandler);
    Future<User> quickRegisterUser(String handle, UserCompletionHandler completionHandler);
    Future<User> login(String login, String password, UserCompletionHandler completionHandler);
    Future<User> get(long id, UserCompletionHandler completionHandler);

    // Todo: Testing only...remove
    void addUser(User user);
}
