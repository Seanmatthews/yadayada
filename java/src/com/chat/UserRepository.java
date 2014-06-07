package com.chat;

import java.io.InvalidObjectException;
import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserRepository {
    Future<UserRepositoryActionResult> registerUser(String login, String password, String handle, String UUID,
                                                    long phoneNumber, String deviceTokenString,
                                                    UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> login(String login, String password, UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> get(long id, UserRepositoryCompletionHandler completionHandler);
    Future<UserRepositoryActionResult> getFromPhone(long phoneNumber, UserRepositoryCompletionHandler handler);
    void addToChatroom(User user, Chatroom chatroom);
    void removeFromChatroom(User user, Chatroom chatroom);
    Iterator<Chatroom> getChatrooms(User user);
    void changeLogin(User user, String login);

    public interface UserRepositoryCompletionHandler {
        void onCompletion(UserRepositoryActionResult user);
    }

    public class UserRepositoryActionResult {
        private final User user;
        private final String message;
        private final UserRepositoryActionResultCode code;
        private final boolean threaded;

        public UserRepositoryActionResult(User user, boolean threaded) {
            this.user = user;
            this.message = "";
            this.code = UserRepositoryActionResultCode.OK;
            this.threaded = threaded;
        }

        public UserRepositoryActionResult(UserRepositoryActionResultCode code, String message, boolean threaded) {
            this.user = null;
            this.message = message;
            this.code = code;
            this.threaded = threaded;
        }

        public UserRepositoryActionResult(UserRepositoryActionResultCode code, String message, User user, boolean threaded) {
            this.user = user;
            this.message = message;
            this.code = code;
            this.threaded = threaded;
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

        public boolean isThreaded() {
            return threaded;
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
