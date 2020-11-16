package com.oldust.sync.serializer;

import java.io.*;
import java.util.Base64;

public class Base64Serializer {

    public static String serialize(Serializable o) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(o);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public static <T> T deserialize(String base64) {
        byte[] decode = Base64.getDecoder().decode(base64);

        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(decode))) {
            return (T) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
