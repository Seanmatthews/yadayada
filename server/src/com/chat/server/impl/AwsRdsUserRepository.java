package com.chat.server.impl;

import com.chat.User;
import com.chat.UserCompletionHandler;
import com.chat.UserRepository;

import java.io.InvalidObjectException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

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
    private final Map<Long, User> idToUserMap = new ConcurrentHashMap<>();

    public AwsRdsUserRepository(String connectionString, String user, String password) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        connect = DriverManager.getConnection(connectionString, user, password);
    }

    @Override
    public Future<User> registerUser(final String login, final String password, final String handle, final UserCompletionHandler completionHandler) {
        try {
            final Statement statement = connect.createStatement();
            final UserFuture future = new UserFuture(statement);

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        User user = null;

                        // insert a new row, we're using uniqueness of login
                        // to accept/reject this
                        int rows = statement.executeUpdate("Insert Into userdb.User (Login, Password, Handle) values ('" +
                                                           login + "','" + password + "','" + handle + "')",
                                                           Statement.RETURN_GENERATED_KEYS);

                        if (rows != 0) {
                            ResultSet generatedKeys = statement.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                long id = generatedKeys.getLong("GENERATED_KEY");
                                user = new User(id, login, password, handle);
                                future.setUser(user);
                            }
                            else {
                                future.setUser(null);
                            }
                        }
                        else {
                            future.setUser(null);
                        }

                        if (completionHandler != null)
                            completionHandler.onCompletion(user);

                    } catch (SQLException e) {
                        e.printStackTrace();

                        if (completionHandler != null)
                            completionHandler.onCompletion(null);
                    }
                }
            });

            return future;
        } catch (SQLException e) {
            e.printStackTrace();
            return new UserFuture(null);
        }
    }

    @Override
    public Future<User> quickRegisterUser(String handle, UserCompletionHandler completionHandler) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<User> login(String login, String password, UserCompletionHandler completionHandler) {
        User user = null;

        try {
            ResultSet results = connect.createStatement().executeQuery("select UserId, Handle from userdb.User where Login = '" + login + "' and Password = '" + password + "'");
            if (results.next()) {
                long id = results.getLong("UserId");
                String handle = results.getString("Handle");
                user = new User(id, login, password, handle);
            }
        }
        catch(SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        completionHandler.onCompletion(user);

        return null;
    }

    @Override
    public Future<User> get(long id, UserCompletionHandler completionHandler) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addUser(User user) throws InvalidObjectException {
        throw new InvalidObjectException("Cannot call addUser for a AwsRdsUserRepository");
    }

    private static class UserFuture implements Future<User> {
        private static final int TIMEOUT_INTERNAL_MS = 10;

        private final Statement statement;
        private volatile boolean userSet;
        private volatile User user;

        public UserFuture(Statement statement) {
            this.statement = statement;
        }

        private void setUser(User user) {
            this.user = user;
            this.userSet = true;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                if (statement.isClosed()) {
                    return true;
                }

                statement.close();
                return true;
            } catch (SQLException e) {
                return true;
            }
        }

        @Override
        public boolean isCancelled() {
            try {
                return statement.isClosed();
            } catch (SQLException e) {
                return true;
            }
        }

        @Override
        public boolean isDone() {
            return userSet;
        }

        @Override
        public User get() throws InterruptedException, ExecutionException {
            while(!userSet) {
                Thread.sleep(TIMEOUT_INTERNAL_MS);
            }

            return user;
        }

        @Override
        public User get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long time = System.currentTimeMillis();
            timeout = time + unit.toMillis(timeout);

            while(!userSet) {
                Thread.sleep(TIMEOUT_INTERNAL_MS);

                if (System.currentTimeMillis() >= timeout) {
                    return null;
                }
            }

            return user;
        }
    }
}
