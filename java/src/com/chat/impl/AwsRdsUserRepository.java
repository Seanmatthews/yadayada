package com.chat.impl;

import com.chat.Chatroom;
import com.chat.User;
import com.chat.UserRepository;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static com.chat.UserRepository.UserRepositoryActionResultCode.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/21/13
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class AwsRdsUserRepository implements UserRepository {
    // SQL connection
    private final Connection connect;

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    // cache
    private final Map<Long, User> idToUserMap = new ConcurrentHashMap<>();
    private final Map<User, Set<Chatroom>> userChatroomMap = new HashMap<>();

    public AwsRdsUserRepository(Connection connection) throws ClassNotFoundException, SQLException {
        this.connect = connection;
    }

    @Override
    public Collection<User> getAllUsers() {
        // TODO
        return null;
    }

    @Override
    public Future<UserRepositoryActionResult> registerUser(final String login, final String password,
                                                           final String handle, final String UUID,
                                                           final long phoneNumber, final String deviceTokenString,
                                                           final UserRepositoryCompletionHandler completionHandler) {
        final UserFuture future = new UserFuture(completionHandler);

        // submit a new thread to contact the database and let us know when we've inserted
        executor.submit(new Runnable() {
            @Override
            public void run() {
                PreparedStatement userExistsStatement = null;

                try {
                    userExistsStatement = connect.prepareStatement("select UserId from userdb.User where Login = ?");
                    future.addStatement(userExistsStatement);

                    // does this user exist?
                    userExistsStatement.setString(1, login);
                    ResultSet set = userExistsStatement.executeQuery();

                    if (set.next()) {
                        // yep, set result and bail out
                        future.setResult(new UserRepositoryActionResult(UserAlreadyExists, login + " is already taken", true));
                        return;
                    }
                } catch (Exception e) {
                    // SQL error
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot check if user exists", true));
                    return;
                } finally {
                    future.removeStatement(userExistsStatement);
                }

                // okay, now we know that a user with this login doesn't exist

                PreparedStatement insertUserStatement = null;

                try {
                    // let's insert!
                    insertUserStatement = connect.prepareStatement("insert into userdb.User (Login, Password, Handle, UUID) values (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    future.addStatement(insertUserStatement);

                    insertUserStatement.setString(1, login);
                    insertUserStatement.setString(2, password);
                    insertUserStatement.setString(3, handle);
                    insertUserStatement.setString(4, UUID);
                    int rowsUpdated = insertUserStatement.executeUpdate();

                    if (rowsUpdated != 0) {
                        ResultSet generatedKeys = insertUserStatement.getGeneratedKeys();

                        if (generatedKeys.next()) {
                            long id = generatedKeys.getLong("GENERATED_KEY");
                            User user = new User(id, UUID, login, password, handle, phoneNumber, deviceTokenString, AwsRdsUserRepository.this);
                            idToUserMap.put(id, user);

                            future.setResult(new UserRepositoryActionResult(user, true));
                        } else {
                            // Hmm weird, we didn't generate a key
                            future.setResult(new UserRepositoryActionResult(ConnectionError, "Unknown connection error", true));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot insert new user", true));
                } finally {
                    future.removeStatement(insertUserStatement);
                }
            }
        });

        return future;
    }

    @Override
    public Future<UserRepositoryActionResult> login(final String login, final String password, UserRepositoryCompletionHandler completionHandler) {
        final UserFuture future = new UserFuture(completionHandler);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                PreparedStatement loginStatement = null;

                try {
                    loginStatement = connect.prepareStatement("select UserId, Handle from userdb.User where Login = ? and Password = ?");
                    future.addStatement(loginStatement);

                    loginStatement.setString(1, login);
                    loginStatement.setString(2, password);
                    ResultSet results = loginStatement.executeQuery();

                    if (results.next()) {
                        long id = results.getLong("UserId");
                        String handle = results.getString("Handle");
                        long phoneNumber = results.getLong("PhoneNumber");
                        String deviceToken = results.getString("DeviceToken");
                        String UUID = results.getString("UUID");
                        User user = new User(id, UUID, login, password, handle, phoneNumber, deviceToken, AwsRdsUserRepository.this);
                        addUser(user);

                        future.setResult(new UserRepositoryActionResult(user, true));
                    }
                    else {
                        // login/password combo doesn't exist
                        future.setResult(new UserRepositoryActionResult(InvalidUserNameOrPassword, "Invalid user name or password", true));
                    }
                } catch(SQLException e) {
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Could not log user in", true));
                } finally {
                    future.removeStatement(loginStatement);
                }
            }
        });

       return future;
    }

    private void addUser(User user) {
        idToUserMap.put(user.getId(), user);
        userChatroomMap.put(user, new HashSet<Chatroom>());
    }

    @Override
    public Future<UserRepositoryActionResult> get(final long id, UserRepositoryCompletionHandler completionHandler) {
        User user = idToUserMap.get(id);

        if (user != null) {
            return new UserFuture(user, completionHandler);
        }

        final UserFuture future = new UserFuture(completionHandler);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                PreparedStatement findStatement = null;

                try {
                    findStatement = connect.prepareStatement("select Login, Handle, PhoneNumber from userdb.User where UserId = ?");
                    future.addStatement(findStatement);

                    findStatement.setLong(1, id);
                    ResultSet results = findStatement.executeQuery();

                    if (results.next()) {
                        String login = results.getString("Login");
                        String handle = results.getString("Handle");
                        long phoneNumber = results.getLong("PhoneNumber");
                        String deviceToken = results.getString("DeviceToken");
                        String UUID = results.getString("UUID");
                        User user2 = new User(id, UUID, login, "<BLANK>", handle, phoneNumber, deviceToken, AwsRdsUserRepository.this);
                        addUser(user2);

                        future.setResult(new UserRepositoryActionResult(user2, true));
                    }
                    else {
                        future.setResult(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserId, "Unknown user id", true));
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot insert new user", true));
                } finally {
                    future.removeStatement(findStatement);
                }
            }
        });

        return future;
    }

    public Future<UserRepositoryActionResult> getFromPhone(long phoneNumber, UserRepositoryCompletionHandler handler) {
        // Stubbed. We don't use this class currently.
        return null;
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
        // TODO
    }

    private static class UserFuture implements Future<UserRepositoryActionResult> {
        private final CountDownLatch latch;
        private volatile UserRepositoryActionResult result;

        private final UserRepositoryCompletionHandler handler;
        private final ConcurrentLinkedQueue<Statement> statements = new ConcurrentLinkedQueue<>();

        public UserFuture(UserRepositoryCompletionHandler handler) {
            this.handler = handler;
            this.latch = new CountDownLatch(1);
        }

        public UserFuture(User user, UserRepositoryCompletionHandler handler) {
            this.result = new UserRepositoryActionResult(user, false);
            this.handler = handler;
            this.latch = new CountDownLatch(0);
        }

        public void setResult(UserRepositoryActionResult result) {
            this.result = result;

            if (handler != null)
                handler.onCompletion(result);

            latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                for (Statement statement : statements) {
                    statement.close();
                }

                return true;
            } catch (SQLException e) {
                return true;
            }
        }

        @Override
        public boolean isCancelled() {
            try {
                for (Statement statement : statements) {
                    if (!statement.isClosed())
                        return false;
                }

                return true;
            } catch (SQLException e) {
                return true;
            }
        }

        @Override
        public boolean isDone() {
            return latch.getCount() > 0;
        }

        @Override
        public UserRepositoryActionResult get() throws InterruptedException, ExecutionException {
            latch.await();
            return result;
        }

        @Override
        public UserRepositoryActionResult get(long timeout, TimeUnit unit) throws InterruptedException {
            latch.await(timeout, unit);
            return result;
        }

        public void addStatement(Statement statement) {
            statements.add(statement);
        }

        public void removeStatement(Statement statement) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }

                statements.remove(statement);
            }
        }
    }
}
