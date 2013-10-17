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
                short length = din.readShort();
                byte msgType = din.readByte();
                MessageTypes type = MessageTypes.lookup(msgType);

                if (type == null) {
                    byte[] msg = new byte[length];
                    din.read(msg);
                    System.out.println("Received unknown msg: " + new String(msg));
                    continue;
                }

                switch (type) {
                    case CREATE_CHATROOM:
                        break;

                    case JOIN_CHATROOM:
                        break;

                    case REGISTER:
                        server.registerUser(socket, din.readUTF(), din.readUTF());
                        break;

                    case LOGIN:
                        server.login(socket, din.readUTF(), din.readUTF());
                        break;

                    case SUBMIT_MESSAGE:
                        long userId = din.readLong();
                        long chatroomId = din.readLong();
                        String message = din.readUTF();
                        System.out.println("Sending " + message);
                        server.newMessage(userId, chatroomId, message);
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
