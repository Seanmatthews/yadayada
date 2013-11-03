package com.chat.client.load;

import com.chat.client.text.ChatTextClient;
import com.chat.msgs.ValidationError;

import java.io.IOException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/3/13
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadTester {
    public static void main(String[] args) throws IOException, InterruptedException, ValidationError {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int numClients = Integer.parseInt(args[2]);

        System.out.println("Starting setup of clients");

        ChatTextClient[] clients = new ChatTextClient[numClients];
        for (int i=0; i<numClients; i++) {
            if (i % 10 == 0) {
                System.out.println("- Starting client " + i);
            }

            clients[i] = new ChatTextClient(host, port, "Load" + i, "Pass" + i);
        }

        Thread.sleep(5000);

        System.out.println("Finished setup up clients");

        Random random = new Random();
        while(true) {
            clients[random.nextInt(numClients)].sendMessage("Hey there");
            Thread.sleep(100);
        }
    }
}
