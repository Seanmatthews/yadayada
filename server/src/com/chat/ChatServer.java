import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
    private final UserRepository userRepo = new UserRepository();
    private final Map<Socket, DataOutputStream> streams = new HashMap<Socket, DataOutputStream>();
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
            streams.put(socket, stream);
            execService.submit(new ChatServerListener(this, socket));
        }
    }

    public void newMessage(long userId, long chatroomId, String message) {
        Message msg = new Message();
        msg.message = message;
        msg.chatroomId = chatroomId;
        msg.id = nextMessageId++;

        User user = userRepo.get(userId);

        if (user == null) {
            System.out.println("Dropping message. Unknown user: " + userId);
            return;
        }

        synchronized (streams) {
            for (DataOutputStream dout : streams.values()) {
                try {
                    dout.writeShort(1 + 8 + 8 + getLength(user.login) + getLength(msg.message));
                    dout.writeByte(MessageTypes.MESSAGE.getId());
                    dout.writeLong(msg.id);
                    dout.writeLong(chatroomId);
                    dout.writeLong(userId);
                    dout.writeUTF(user.login);
                    dout.writeUTF(msg.message);
                } catch (IOException e) {
                    System.out.println("Error writing to " + dout);
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeConnection(Socket socket) {
        System.out.println("Removing connection to " + socket);

        synchronized (streams) {
            streams.remove(socket);

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing " + socket);
                e.printStackTrace();
            }
        }
    }

    public void registerUser(Socket socket, String login, String password) {
        User user = userRepo.registerUser(login, password);

        synchronized (streams) {
            DataOutputStream dataOutputStream = streams.get(socket);

            try {
                if (user == null) {
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
                System.out.println("Error writing to client when registering user");
                removeConnection(socket);
            }
        }
    }

    public void login(Socket socket, String login, String password) {
        User user = userRepo.login(login, password);

        synchronized (streams) {
            DataOutputStream dataOutputStream = streams.get(socket);

            try {
                if (user == null) {
                    String msg = "Invalid user or password: " + login;
                    dataOutputStream.writeShort(1 + getLength(msg));
                    dataOutputStream.writeByte(MessageTypes.LOGIN_REJECT.getId());
                    dataOutputStream.writeUTF(msg);
                    return;
                }

                dataOutputStream.writeShort(1 + 8);
                dataOutputStream.writeByte(MessageTypes.LOGIN_ACCEPT.getId());
                dataOutputStream.writeLong(user.id);
            } catch (IOException e) {
                removeConnection(socket);
            }
        }
    }

    private int getLength(String str) {
        return 2 + str.length();
    }
}
