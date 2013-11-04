package com.chat.client.load;

import com.chat.client.text.ChatTextClient;
import com.chat.msgs.ValidationError;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        int startClient = Integer.parseInt(args[3]);

        System.out.println("Starting setup of clients");

        ExecutorService exec = Executors.newCachedThreadPool();

        CountDownLatch countDownLatch = new CountDownLatch(numClients);

        LoadTesterClient[] clients = new LoadTesterClient[numClients];
        for (int i=0; i<numClients; i++) {
            if (i % 10 == 0) {
                System.out.println("- Starting client " + i);
            }

            clients[i] = new LoadTesterClient(host, port, "Load" + i + startClient, "Pass" + i, countDownLatch);
            exec.execute(clients[i]);
        }

        while(true) {
            countDownLatch.await(1, TimeUnit.SECONDS);
            long count = countDownLatch.getCount();
            System.out.println("Latch - " + count);
            if (count == 0)
                break;
        }

        Random random = new Random();
        while(true) {
            int i = random.nextInt(numClients);
            System.out.println("Client " + i);
            clients[i].sendMessage("Hey there");
            Thread.sleep(10);
        }
    }
}
