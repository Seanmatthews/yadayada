import java.io.IOException;

public class ChatServerMain {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        new ChatServer(port);
    }
}
