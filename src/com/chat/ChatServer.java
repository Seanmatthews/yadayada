import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServer {
    private final Map<String, User> users = new HashMap<String, User>();
    private final Map<Long, User> idToUser = new HashMap<Long, User>();
    private long nextUserId = 1;

    private final Map<Socket, DataOutputStream> socketStreamMap = new HashMap<Socket, DataOutputStream>();
    private final ExecutorService execService = Executors.newFixedThreadPool(100);
    private ServerSocket serverSocket;
    private long nextMessageId = 1;

    public ChatServer(int port) throws IOException {
        listen(port);
    }

    private void listen(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        System.out.println("Listening on: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection from: " + socket);

            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
            socketStreamMap.put(socket, stream);
            execService.submit(new ChatServerListener(this, socket));
        }
    }

    public void newMessage(long userId, String message) {
        Message msg = new Message();
        msg.message = message;
        msg.id = nextMessageId++;

        User user = idToUser.get(userId);

        synchronized (socketStreamMap) {
            for (DataOutputStream dataOutputStream : socketStreamMap.values()) {
                try {
                    dataOutputStream.writeShort(1 + 8 + 8 + getLength(user.login) + getLength(msg.message));
                    dataOutputStream.writeByte(MessageTypes.MESSAGE.getId());
                    dataOutputStream.writeLong(msg.id);
                    dataOutputStream.writeLong(userId);
                    dataOutputStream.writeUTF(user.login);
                    dataOutputStream.writeUTF(msg.message);
                } catch (IOException e) {
                    System.out.println("Error writing to " + dataOutputStream);
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeConnection(Socket socket) {
        System.out.println("Removing connection to " + socket);

        synchronized (socketStreamMap) {
            socketStreamMap.remove(socket);

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing " + socket);
                e.printStackTrace();
            }
        }
    }

    public void registerUser(Socket socket, User user) {
        User existingUser = users.get(user.login);

        long id = 0;
        if (existingUser == null) {
            user.id = nextUserId++;
            users.put(user.login, user);
            idToUser.put(user.id, user);
            id = user.id;
        }

        synchronized (socketStreamMap) {
            DataOutputStream dataOutputStream = socketStreamMap.get(socket);

            try {
                if (id == 0) {
                    String msg = "Registration failure. " + user.login + " already exists";
                    dataOutputStream.writeShort(1 + getLength(msg));
                    dataOutputStream.writeByte(MessageTypes.REGISTER_REJECT.getId());
                    dataOutputStream.writeUTF(msg);
                }
                else {
                    dataOutputStream.writeShort(1 + 8);
                    dataOutputStream.writeByte(MessageTypes.REGISTER_ACCEPT.getId());
                    dataOutputStream.writeLong(user.id);
                }
            } catch (IOException e) {
                removeConnection(socket);
            }
        }
    }

    public void login(Socket socket, User user) {
        User existingUser = users.get(user.login);

        synchronized (socketStreamMap) {
            DataOutputStream dataOutputStream = socketStreamMap.get(socket);

            try {
                if (existingUser == null) {
                    String msg = "Invalid user: " + user.login;
                    dataOutputStream.writeShort(1 + getLength(msg));
                    dataOutputStream.writeByte(MessageTypes.LOGIN_REJECT.getId());
                    dataOutputStream.writeUTF(msg);
                    return;
                }

                if (!existingUser.password.equals(user.password)) {
                    String msg = "Invalid password";
                    dataOutputStream.writeShort(1 + getLength(msg));
                    dataOutputStream.writeByte(MessageTypes.LOGIN_REJECT.getId());
                    dataOutputStream.writeUTF(msg);
                    return;
                }

                dataOutputStream.writeShort(1 + 8);
                dataOutputStream.writeByte(MessageTypes.LOGIN_ACCEPT.getId());
                dataOutputStream.writeLong(existingUser.id);
            } catch (IOException e) {
                removeConnection(socket);
            }
        }
    }

    private int getLength(String str) {
        return 2 + str.length();
    }
}
