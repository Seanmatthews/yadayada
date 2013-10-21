package com.chat.server.impl;

import com.chat.User;
import com.chat.UserCompletionHandler;
import com.chat.UserRepository;

import java.util.Map;
import java.util.concurrent.*;

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

    public Future<User> registerUser(String login, String password, String handle, UserCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null) {
            if (handler != null)
                handler.onCompletion(user);

            return new UserFuture(null);
        }

        user = new User(nextUserId++, login, password, handle);

        loginToUserMap.put(user.getLogin(), user);
        idToUserMap.put(user.getId(), user);

        if (handler != null)
            handler.onCompletion(user);

        return new UserFuture(user);
    }

    @Override
    public Future<User> quickRegisterUser(String handle, UserCompletionHandler handler) {
        User user = new User(nextUserId++, "<QUICK>", "<QUICK>", handle);

        idToUserMap.put(user.getId(), user);

        if (handler != null)
            handler.onCompletion(user);

        return new UserFuture(user);
    }

    public Future<User> login(String login, String password, UserCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // don't know this user
        if (user == null || !user.getPassword().equals(password)) {
            user = null;
        }

        if (handler != null)
            handler.onCompletion(user);

        return new UserFuture(user);
    }

    public Future<User> get(long id, UserCompletionHandler handler) {
        User user = idToUserMap.get(id);

        if (handler != null)
            handler.onCompletion(user);

        return new UserFuture(user);
    }

    @Override
    public void addUser(User user) {
        idToUserMap.put(user.getId(), user);
    }

    private static class UserFuture implements Future<User> {
        private final User user;

        public UserFuture(User user) {
            this.user = user;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public User get() throws InterruptedException, ExecutionException {
            return user;
        }

        @Override
        public User get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return user;
        }
    }
}
