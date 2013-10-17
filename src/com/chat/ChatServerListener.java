import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerListener implements Runnable {
    private final ChatServer server;
    private final Socket socket;


    public ChatServerListener(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream din = new DataInputStream( socket.getInputStream() );

            while(true) {
                byte msgType = din.readByte();
                MessageTypes type = MessageTypes.lookup(msgType);

                switch (type) {
                    case REGISTER:
                        User u = new User();
                        u.login = din.readUTF();
                        u.password = din.readUTF();
                        server.registerUser(socket, u);
                        break;

                    case LOGIN:
                        User user = new User();
                        user.login = din.readUTF();
                        user.password = din.readUTF();
                        server.login(socket, user);
                        break;

                    case SUBMIT_MESSAGE:
                        long userId = din.readLong();
                        String message = din.readUTF();
                        System.out.println("Sending " + message);
                        server.newMessage(userId, message);
                        break;
                }
            }
        } catch (EOFException e) {
            // nothing
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeConnection(socket);
        }
    }
}
