package com.chat.client.text;

import com.chat.client.ChatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatTextInput implements Runnable {
    private final ChatTextClient client;

    public ChatTextInput(ChatTextClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            while(true) {
                String msg = br.readLine();
                client.sendMessage(msg);
            }
        } catch(IOException e) {
            System.out.println("Error reading");
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}