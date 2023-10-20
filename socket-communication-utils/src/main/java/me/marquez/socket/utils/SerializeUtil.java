package me.marquez.socket.utils;

import java.io.*;

public class SerializeUtil {

    public static String objectToHexString(Object object) {
        return encode(objectToByteArray(object));
    }

    public static Object hexStringToObject(String hex) {
        return byteArrayToObject(decode(hex));
    }

    public static byte[] objectToByteArray(Object object) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(object);
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object byteArrayToObject(byte[] bytes) {
        InputStream byteStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return objectStream.readObject();
        }catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(byte[] data) {
        char[] encoded = new char[data.length * 2];

        for(int i = 0; i < data.length; ++i) {
            encoded[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 240) >>> 4, 16));
            encoded[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 15, 16));
        }

        return new String(encoded);
    }

    public static byte[] decode(String hex) {
        byte[] decoded = new byte[hex.length() / 2];

        for(int i = 0; i < decoded.length; ++i) {
            decoded[i] = (byte)((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
        }

        return decoded;
    }

}
