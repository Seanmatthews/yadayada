package com.chat.image;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
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

    private int[] sizes = new int[] { 53, 53,
                                      106, 106 };

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

            for(int i=0; i< sizes.length; i+=2) {
                int width = sizes[i];
                int height = sizes[i + 1];

                ByteArrayOutputStream os2 = transformImage(image, width, height);
                uploadFile(os2, userId, width + "x" + height);
            }
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
        BufferedImage output = Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS);
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

        String file = userId + "-" + suffix + ".jpeg";
        uploader.upload(file, is, meta);
        System.out.println("- Uploaded " + file);
    }
}
