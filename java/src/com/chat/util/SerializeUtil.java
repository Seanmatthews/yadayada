package com.chat.util;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Created by sean on 6/19/14.
 */
public class SerializeUtil {
    // deserialize to Object from given file
    public static Object deserialize(String fileName) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }

    // serialize the given object and save it to file
    public static void serialize(Object obj, String fileName)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);

        fos.close();
    }

    public static void setPrivateFinalVar(Object obj, String objVarName, Object newValue) {
        try {
            Field f = obj.getClass().getDeclaredField(objVarName);
            f.setAccessible(true);
            f.set(obj, newValue);
        }
        catch (NoSuchFieldException nsfe) {

        }
        catch (IllegalAccessException iae) {

        }
    }
}
