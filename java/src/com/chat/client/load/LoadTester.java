package com.chat.client.load;

import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.ValidationError;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        final EventService eventService = new EventServiceImpl();
        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();

        System.out.println("Starting setup of clients " + startClient + " -> " + (startClient + numClients - 1));

        List<LoadTesterClient> clients = new ArrayList<>(numClients);
        for (int i=0; i<numClients; i++) {
            int userNum = i + startClient;

            if (userNum % 100 == 0) {
                System.out.println("- Starting client " + i + " - " + userNum);
            }

            clients.add(new LoadTesterClient(eventService, host, port, "Load" + userNum, "Pass" + userNum, chatroomRepo, userRepo));
        }

        boolean allReady = false;
        Random random = new Random();
        int sentMessages = 0;
        long lastTimestamp = 0;

        Executors.newCachedThreadPool().execute(
            new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            eventService.wakeup();
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            });

        while(true) {
            eventService.runOnce();

            int ready = 0;
            int error = 0;
            int oldMessages = 0;

            if (!allReady) {
                for (LoadTesterClient client : clients) {
                    if (client.getState() == LoadTesterClient.LoginState.Error)
                        error++;

                    if (client.getState() == LoadTesterClient.LoginState.JoinedChatroom)
                        ready++;
                }

                if (ready + error >= numClients) {
                    allReady = true;
                    lastTimestamp = System.currentTimeMillis();
                }

                System.out.println("Ready: " + ready + ", Error: " + error);
            }
            else {
                int i = random.nextInt(clients.size());
                LoadTesterClient client = clients.get(i);

                if (client.isAlive()) {
                    client.sendMessage("Hey there");
                    sentMessages++;

                    if (sentMessages % 1000 == 0) {
                        long newTimestamp = System.currentTimeMillis();

                        double timeDiffSec = (newTimestamp - lastTimestamp) / 1000.0;
                        System.out.println("Sent " + sentMessages + " messages @ " + 1000/timeDiffSec + " msgs/s");

                        int messages = 0;
                        for (LoadTesterClient c : clients) {
                            if (c.isAlive()) {
                                messages += client.getMessagesRecv();
                            }
                        }

                        System.out.println("Recv " + messages + " messages @ " + (messages - oldMessages)/timeDiffSec + " msgs/s");
                        oldMessages = messages;
                    }
                }
            }
        }
    }
}

