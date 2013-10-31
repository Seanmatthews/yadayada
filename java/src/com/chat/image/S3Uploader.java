package com.chat.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/25/13
 * Time: 8:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class S3Uploader implements Uploader {
    private final AmazonS3Client client;
    private final String bucket;

    public S3Uploader(AmazonS3Client client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public void upload(String name, InputStream stream, ObjectMetadata meta) {
        client.putObject(bucket, name, stream, meta);
    }
}
