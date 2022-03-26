package com.wenxia.swift.common.util;

import java.io.*;

/**
 * @author zhouw
 * @date 2022-03-26
 */
public class Serializer {

    public static byte[] serialize(Object object) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(object);
            return os.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) throws ClassNotFoundException, IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            ObjectInputStream ois = new ObjectInputStream(in);
            return (T) ois.readObject();
        }
    }
}
