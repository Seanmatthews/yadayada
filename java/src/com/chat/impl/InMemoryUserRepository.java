package com.chat.impl;

import com.chat.Chatroom;
import com.chat.User;
import com.chat.UserRepository;

import java.util.*;
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
    private long nextUserId = 1;

    private final Map<Long, User> idToUserMap = new HashMap<>();
    private final Map<String, User> loginToUserMap = new HashMap<>();
    private final Map<Long, User> phoneToUserMap = new HashMap<>();
    private final Map<User, Set<Chatroom>> userChatroomMap = new HashMap<>();

    public Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, String UUID,
                                                           long phoneNumber, String deviceTokenString,
                                                           UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null) {
            UserRepositoryActionResult result = new UserRepositoryActionResult(UserAlreadyExists, "User already exists", user, false);
            return new UserFuture(result, handler);
        }

        user = new User(nextUserId++, UUID, login, password, handle, phoneNumber, deviceTokenString, this);

        addUser(user);

        return new UserFuture(new UserRepositoryActionResult(user, false), handler);
    }

    public Future<UserRepositoryActionResult> login(String login, String password, UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // don't know this user
        if (user == null || !user.getPassword().equals(password)) {
            return new UserFuture(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserNameOrPassword, "Invalid username or password", false), handler);
        }

        return new UserFuture(new UserRepositoryActionResult(user, false), handler);
    }

    public Future<UserRepositoryActionResult> get(long id, UserRepositoryCompletionHandler handler) {
        User user = idToUserMap.get(id);

        if (user == null) {
            return new UserFuture(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserId, "Unknown user id " + id, false), handler);
        }

        return new UserFuture(new UserRepositoryActionResult(user, false), handler);
    }

    public Future<UserRepositoryActionResult> getFromPhone(long phoneNumber, UserRepositoryCompletionHandler handler) {
        User user = phoneToUserMap.get(phoneNumber);

        if (user == null) {
            return new UserFuture(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserId, "Unknown user phone number " + phoneNumber, false), handler);
        }

        return new UserFuture(new UserRepositoryActionResult(user, false), handler);
    }


    public void addUser(User user) {
        // This seems OK to handle device re-register, as long as logins are unique
        loginToUserMap.put(user.getLogin(), user);
        idToUserMap.put(user.getId(), user);
        phoneToUserMap.put(user.getPhoneNumber(), user);
        userChatroomMap.put(user, Collections.newSetFromMap(new HashMap<Chatroom, Boolean>()));
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

    @Override
    public void changeLogin(User user, String login) {
        loginToUserMap.remove(user.getLogin());
        loginToUserMap.put(login, user);
        user.setLogin(login);
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
