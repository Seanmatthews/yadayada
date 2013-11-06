package com.chat.impl;

import com.chat.Chatroom;
import com.chat.User;
import com.chat.UserRepository;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.chat.UserRepository.UserRepositoryActionResultCode.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryUserRepository implements UserRepository {
    private final Logger logger;

    private AtomicLong nextUserId = new AtomicLong(1);

    private final Map<Long, User> idToUserMap = new ConcurrentHashMap<>();
    private final Map<String, User> loginToUserMap = new ConcurrentHashMap<>();
    private final Map<User, Set<Chatroom>> userChatroomMap = new ConcurrentHashMap<>();

    public InMemoryUserRepository(Logger logger) {
        this.logger = logger;
    }

    public Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, String UUID, UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null) {
            UserRepositoryActionResult result = new UserRepositoryActionResult(UserAlreadyExists, "User already exists");
            return new UserFuture(result, handler);
        }

        user = new User(nextUserId.getAndIncrement(), login, password, handle, this);

        addUser(user);

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

    public void addUser(User user) {
        loginToUserMap.put(user.getLogin(), user);
        idToUserMap.put(user.getId(), user);
        userChatroomMap.put(user, Collections.newSetFromMap(new ConcurrentHashMap<Chatroom, Boolean>()));
    }

    @Override
    public void addToChatroom(User user, Chatroom chatroom) {
        userChatroomMap.get(user).add(chatroom);
    }

    @Override
    public void removeFromChatroom(User user, Chatroom chatroom) {
        userChatroomMap.get(user).remove(chatroom);
    }

    @Override
    public Iterator<Chatroom> getChatrooms(User user) {
        return userChatroomMap.get(user).iterator();
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
