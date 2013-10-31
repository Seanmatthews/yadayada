package com.chat.image;

import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/25/13
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Uploader {
    void upload(String name, InputStream stream, ObjectMetadata meta);
}
