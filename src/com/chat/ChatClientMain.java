import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientMain {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[1]);
        new ChatClient(args[0], port, args[2], args[3]);
    }
}
