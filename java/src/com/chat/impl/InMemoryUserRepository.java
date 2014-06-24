package com.chat.impl;

import com.chat.Chatroom;
import com.chat.User;
import com.chat.UserRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
public class InMemoryUserRepository implements UserRepository, Serializable {

    // Changing this will make files serialized before the change incompatible
    private static final long serialVersionUID = -6470090944414208495L;

//    private long nextUserId = 1;
    private AtomicLong nextUserId = new AtomicLong(1);
    private Map<Long, User> idToUserMap = new HashMap<>();
    private Map<String, User> loginToUserMap = new HashMap<>();
    private Map<Long, User> phoneToUserMap = new HashMap<>();
    private Map<User, Set<Chatroom>> userChatroomMap = new HashMap<>();

    public Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, String UUID,
                                                           long phoneNumber, String deviceTokenString,
                                                           UserRepositoryCompletionHandler handler) {
        User user = loginToUserMap.get(login);

        // already registered
        if (user != null) {
            UserRepositoryActionResult result = new UserRepositoryActionResult(UserAlreadyExists, "User already exists", user, false);
            return new UserFuture(result, handler);
        }

        user = new User(nextUserId.getAndIncrement(), UUID, login, password, handle, phoneNumber, deviceTokenString, this);

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
    public Collection<User> getAllUsers() {
        return idToUserMap.values();
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

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

        out.writeLong(nextUserId.get());
        out.writeObject(idToUserMap);
        out.writeObject(loginToUserMap);
        out.writeObject(phoneToUserMap);
        out.writeObject(userChatroomMap);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();

        nextUserId = new AtomicLong(in.readLong());
        idToUserMap = new HashMap<>((HashMap<Long, User>)in.readObject());
        loginToUserMap = new HashMap<>((HashMap<String, User>)in.readObject());
        phoneToUserMap = new HashMap<>((HashMap<Long, User>)in.readObject());
        userChatroomMap = new HashMap<>((HashMap<User, Set<Chatroom>>)in.readObject());
    }
}
