package com.chat.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/25/13
 * Time: 8:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploaderClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String file = args[2];

        Socket socket = new Socket(host, port);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        System.out.println("Connected to " + socket);

        BufferedImage read = ImageIO.read(new FileInputStream(file));

        System.out.println("Read file");

        os.writeLong(1);
        ImageIO.write(read, "jpeg", os);

        System.out.println("Wrote file");
        Thread.sleep(1000);
    }
}
