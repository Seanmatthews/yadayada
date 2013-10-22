package com.chat.server.impl;

import com.chat.User;
import com.chat.UserRepository;

import java.util.Map;
import java.util.concurrent.*;

import static com.chat.UserRepository.UserRepositoryActionResultCode.*;

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

    public Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null) {
            UserRepositoryActionResult result = new UserRepositoryActionResult(UserAlreadyExists, "User already exists");
            return new UserFuture(result, handler);
        }

        user = new User(nextUserId++, login, password, handle);

        loginToUserMap.put(user.getLogin(), user);
        idToUserMap.put(user.getId(), user);

        return new UserFuture(new UserRepositoryActionResult(user), handler);
    }

    @Override
    public Future<UserRepositoryActionResult> quickRegisterUser(String handle, UserRepositoryCompletionHandler handler) {
        User user = new User(nextUserId++, handle);

        idToUserMap.put(user.getId(), user);

        return new UserFuture(new UserRepositoryActionResult(user), handler);
    }

    public Future<UserRepositoryActionResult> login(String login, String password, UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // don't know this user
        if (user == null || !user.getPassword().equals(password)) {
            return new UserFuture(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserNameOrPassword, "Invalid username or password"), handler);
        }

        return new UserFuture(new UserRepositoryActionResult(user), handler);
    }

    public Future<UserRepositoryActionResult> get(long id, UserRepositoryCompletionHandler handler) {
        User user = idToUserMap.get(id);

        if (user == null) {
            return new UserFuture(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserId, "Unknown user id " + id), handler);
        }

        return new UserFuture(new UserRepositoryActionResult(user), handler);
    }

    @Override
    public void addUser(User user) {
        idToUserMap.put(user.getId(), user);
    }

    private static class UserFuture implements Future<UserRepositoryActionResult> {
        private final UserRepositoryActionResult user;

        public UserFuture(UserRepositoryActionResult user, UserRepositoryCompletionHandler handler) {
            this.user = user;

            if (handler != null)
                handler.onCompletion(user);
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
        public UserRepositoryActionResult get() throws InterruptedException, ExecutionException {
            return user;
        }

        @Override
        public UserRepositoryActionResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return user;
        }
    }
}
