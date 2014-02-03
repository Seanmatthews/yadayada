package com.chat.image;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/24/13
 * Time: 10:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class S3ImageUploader {

    public S3ImageUploader(int port, AWSCredentials cred, String bucket) throws IOException {

        AmazonS3Client client = new AmazonS3Client(cred);
        S3Uploader uploader = new S3Uploader(client, bucket);
        uploader = new S3Uploader(client, bucket);
        ExecutorService execService = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Listening on: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection from: " + socket);
            execService.submit(new ImageStreamListener(socket.getInputStream(), uploader));
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String bucket = args[1];
        String username = args[2];
        String password = args[3];

        new S3ImageUploader(port, new BasicAWSCredentials(username, password), bucket);
    }

}
