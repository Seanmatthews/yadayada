package com.chat.server.impl;

import com.chat.User;
import com.chat.UserRepository;

import java.io.InvalidObjectException;
import java.sql.*;
import java.util.Map;
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
    private final Connection connect;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    // cache
    private final Map<Long, User> idToUserMap = new ConcurrentHashMap<>();

    public AwsRdsUserRepository(Connection connection) throws ClassNotFoundException, SQLException {
        this.connect = connection;
    }

    @Override
    public Future<UserRepositoryActionResult> registerUser(final String login, final String password, final String handle, final String UUID, final UserRepositoryCompletionHandler completionHandler) {
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
                        future.setResult(new UserRepositoryActionResult(UserAlreadyExists, login + " is already taken"));
                        return;
                    }
                } catch (Exception e) {
                    // SQL error
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot check if user exists"));
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
                            User user = new User(id, login, password, handle);
                            idToUserMap.put(id, user);

                            future.setResult(new UserRepositoryActionResult(user));
                        } else {
                            // Hmm weird, we didn't generate a key
                            future.setResult(new UserRepositoryActionResult(ConnectionError, "Unknown connection error"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot insert new user"));
                } finally {
                    future.removeStatement(insertUserStatement);
                }
            }
        });

        return future;
    }

    @Override
    public Future<UserRepositoryActionResult> quickRegisterUser(final String handle, final String UUID, UserRepositoryCompletionHandler completionHandler) {
        final UserFuture future = new UserFuture(completionHandler);

        // submit a new thread to contact the database and let us know when we've inserted
        executor.submit(new Runnable() {
            @Override
            public void run() {
                // okay, now we know that a user with this login doesn't exist
                PreparedStatement insertUserStatement = null;

                try {
                    // let's insert!
                    insertUserStatement = connect.prepareStatement("insert into userdb.User (Handle, UUID) values (?,?)", Statement.RETURN_GENERATED_KEYS);
                    future.addStatement(insertUserStatement);

                    insertUserStatement.setString(1, handle);
                    insertUserStatement.setString(2, UUID);
                    int rowsUpdated = insertUserStatement.executeUpdate();

                    if (rowsUpdated != 0) {
                        ResultSet generatedKeys = insertUserStatement.getGeneratedKeys();

                        if (generatedKeys.next()) {
                            long id = generatedKeys.getLong("GENERATED_KEY");
                            User user = new User(id, handle);
                            idToUserMap.put(id, user);

                            future.setResult(new UserRepositoryActionResult(user));
                        } else {
                            // Hmm weird, we didn't generate a key
                            future.setResult(new UserRepositoryActionResult(ConnectionError, "Unknown connection error"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot insert new user"));
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
                        User user = new User(id, login, password, handle);

                        future.setResult(new UserRepositoryActionResult(user));
                    }
                    else {
                        // login/password combo doesn't exist
                        future.setResult(new UserRepositoryActionResult(InvalidUserNameOrPassword, "Invalid user name or password"));
                    }
                } catch(SQLException e) {
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Could not log user in"));
                } finally {
                    future.removeStatement(loginStatement);
                }
            }
        });

       return future;
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
                    findStatement = connect.prepareStatement("select Login, Handle from userdb.User where UserId = ?");
                    future.addStatement(findStatement);

                    findStatement.setLong(1, id);
                    ResultSet results = findStatement.executeQuery();

                    if (results.next()) {
                        String login = results.getString("Login");
                        String handle = results.getString("Handle");
                        User user2 = new User(id, login, "<BLANK>", handle);
                        idToUserMap.put(id, user2);

                        future.setResult(new UserRepositoryActionResult(user2));
                    }
                    else {
                        future.setResult(new UserRepositoryActionResult(UserRepositoryActionResultCode.InvalidUserId, "Unknown user id"));
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                    future.setResult(new UserRepositoryActionResult(ConnectionError, "Cannot insert new user"));
                } finally {
                    future.removeStatement(findStatement);
                }
            }
        });

        return future;
    }

    @Override
    public void addUser(User user) throws InvalidObjectException {
        throw new InvalidObjectException("Cannot call addUser for a AwsRdsUserRepository");
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
            this.result = new UserRepositoryActionResult(user);
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
