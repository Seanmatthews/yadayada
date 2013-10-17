import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClient {
    private final Socket socket;
    private final DataInputStream dataInput;
    private final DataOutputStream dataOutput;
    private long userId;

    public ChatClient(String host, int port, String user, String password) throws IOException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        dataInput = new DataInputStream(socket.getInputStream());
        dataOutput = new DataOutputStream(socket.getOutputStream());

        registerAndLogin(user, password);
        Executors.newSingleThreadExecutor().submit(new ChatClientInput(this));
        readChat();
    }

    private void registerAndLogin(String user, String password) {
        try {
            System.out.println("Registering user: " + user);
            dataOutput.writeShort( 1 + getLength(user) + getLength(password) );
            dataOutput.writeByte(MessageTypes.REGISTER.getId());
            dataOutput.writeUTF(user);
            dataOutput.writeUTF(password);

            dataInput.readShort(); // size
            MessageTypes msgType = MessageTypes.lookup(dataInput.readByte());
            switch(msgType) {
                case REGISTER_ACCEPT:
                    this.userId = dataInput.readLong();
                    System.out.println("Registration accepted. UserId: " + user);
                    break;
                case REGISTER_REJECT:
                    String msg = dataInput.readUTF();
                    System.out.println("Failed to register user: " + msg);
                    break;
            }

            System.out.println("Logging in user: " + user);
            dataOutput.writeShort( 1 + getLength(user) + getLength(password) );
            dataOutput.writeByte(MessageTypes.LOGIN.getId());
            dataOutput.writeUTF(user);
            dataOutput.writeUTF(password);

            dataInput.readShort(); // size
            msgType = MessageTypes.lookup(dataInput.readByte());
            switch(msgType) {
                case LOGIN_ACCEPT:
                    this.userId = dataInput.readLong();
                    System.out.println("Login accepted. UserId: " + userId);
                    break;
                case LOGIN_REJECT:
                    String msg = dataInput.readUTF();
                    System.out.println("Login rejected: " + msg);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void readChat() {
        try {
           while(true) {
                dataInput.readShort(); // size
                byte messageType = dataInput.readByte();
                MessageTypes msgType = MessageTypes.lookup(messageType);

                switch(msgType) {
                    case MESSAGE:
                        long msgID = dataInput.readLong();
                        long userId = dataInput.readLong();
                        long chatroomId = dataInput.readLong();
                        String userName = dataInput.readUTF();
                        String message = dataInput.readUTF();
                        System.out.println(userName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading chat");
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public void sendMessage(String message) throws IOException {
        dataOutput.writeShort(1 + 8 + getLength(message));
        dataOutput.writeByte(MessageTypes.SUBMIT_MESSAGE.getId());
        dataOutput.writeLong(userId);
        dataOutput.writeLong(1);
        dataOutput.writeUTF(message);
    }

    private int getLength(String str) {
        return 2 + str.length();
    }
}
