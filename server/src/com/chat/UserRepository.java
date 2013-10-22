package com.chat;

import java.io.InvalidObjectException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserRepository {
    Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> quickRegisterUser(String handle, UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> login(String login, String password, UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> get(long id, UserRepositoryCompletionHandler completionHandler);

    // Todo: Testing only...remove
    void addUser(User user) throws InvalidObjectException;

    public interface UserRepositoryCompletionHandler {
        void onCompletion(UserRepositoryActionResult user);
    }

    public class UserRepositoryActionResult {
        private final User user;
        private final String message;
        private final UserRepositoryActionResultCode code;

        public UserRepositoryActionResult(User user) {
            this.user = user;
            this.message = "";
            this.code = UserRepositoryActionResultCode.OK;
        }

        public UserRepositoryActionResult(UserRepositoryActionResultCode code, String message) {
            this.user = null;
            this.message = message;
            this.code = code;
        }

        public User getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        public UserRepositoryActionResultCode getCode() {
            return code;
        }
    }

    public static enum UserRepositoryActionResultCode {
        OK,
        ConnectionError,
        UserAlreadyExists,
        InvalidUserNameOrPassword,
        InvalidUserId
    }
}
