package com.chat.image;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/24/13
 * Time: 10:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageStreamListener implements Runnable {
    private final BufferedInputStream inputStream;
    private final Uploader uploader;

    public ImageStreamListener(InputStream is, Uploader upload) throws IOException {
        inputStream = new BufferedInputStream(is);
        uploader = upload;
    }

    @Override
    public void run() {
        try {
            System.out.println("Receiving image");

            long userId = new DataInputStream(inputStream).readLong();
            BufferedImage image = ImageIO.read( inputStream );

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", os);
            uploadFile(os, userId, "original");

            System.out.println("- Uploaded original");

            ByteArrayOutputStream os2 = transformImage(image, 160, 160);
            uploadFile(os2, userId, "160x160");

            System.out.println("- Uploaded 160x160");

            ByteArrayOutputStream os3 = transformImage(image, 80, 80);
            uploadFile(os3, userId, "80x80");

            System.out.println("- Uploaded 80x80");
        } catch (IOException e) {
            System.err.println("Error uploading");
            e.printStackTrace();
        } catch(AmazonClientException e) {
            System.err.println("Error connecting to Amazon");
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ByteArrayOutputStream transformImage(BufferedImage image, int width, int height) throws IOException {
        AffineTransform transform = new AffineTransform();
        transform.setToScale(1.0 * width / image.getWidth(), 1.0 * height / image.getHeight());
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage output = op.filter(image, null);
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        ImageIO.write(output, "jpeg", os2);
        return os2;
    }

    private void uploadFile(ByteArrayOutputStream os, long userId, String suffix) {
        byte[] imageBuffer = os.toByteArray();
        InputStream is = new ByteArrayInputStream(imageBuffer);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("image/jpeg");
        meta.setContentLength(imageBuffer.length);

        uploader.upload(userId + "-" + suffix + ".jpeg", is, meta);
    }
}
