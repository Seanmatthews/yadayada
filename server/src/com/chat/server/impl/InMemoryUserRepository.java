package com.chat.server.impl;

import com.chat.User;
import com.chat.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryUserRepository implements UserRepository {
    private long nextUserId = 1;

    private final Map<String, User> loginToUserMap = new HashMap<>();
    private final Map<Long, User> idToUserMap = new HashMap<>();

    public User registerUser(String login, String password) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null)
            return null;

        user = new User();
        user.id = nextUserId++;
        user.login = login;
        user.password = password;

        loginToUserMap.put(user.login, user);
        idToUserMap.put(user.id, user);

        return user;
    }

    public User login(String login, String password) {
        User user = loginToUserMap.get(login);

        // don't know this user
        if (user == null)
            return null;

        if (!user.password.equals(password))
            return null;

        return user;
    }

    public User get(long id) {
        return idToUserMap.get(id);
    }

    @Override
    public void addUser(User user) {
        loginToUserMap.put(user.login, user);
        idToUserMap.put(user.id, user);
    }
}
