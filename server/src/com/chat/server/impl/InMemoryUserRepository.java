package com.chat.server.impl;

import com.chat.User;
import com.chat.UserRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryUserRepository implements UserRepository {
    private volatile long nextUserId = 1;

    private final Map<String, User> loginToUserMap = new ConcurrentHashMap<>();
    private final Map<Long, User> idToUserMap = new ConcurrentHashMap<>();

    public User registerUser(String login, String password, String handle) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null)
            return null;

        user = new User(nextUserId++, login, password, handle);

        loginToUserMap.put(user.getLogin(), user);
        idToUserMap.put(user.getId(), user);

        return user;
    }

    @Override
    public User quickRegisterUser(String handle) {
        User user = new User(nextUserId++, "<QUICK>", "<QUICK>", handle);

        idToUserMap.put(user.getId(), user);

        return user;
    }

    public User login(String login, String password) {
        User user = loginToUserMap.get(login);

        // don't know this user
        if (user == null)
            return null;

        if (!user.getPassword().equals(password))
            return null;

        return user;
    }

    public User get(long id) {
        return idToUserMap.get(id);
    }

    @Override
    public void addUser(User user) {
        idToUserMap.put(user.getId(), user);
    }
}
