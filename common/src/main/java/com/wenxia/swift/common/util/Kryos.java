package com.wenxia.swift.common.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author zhouw
 * @date 2022-03-16
 */
public class Kryos {

    private static final ThreadLocal<Kryo> KRYOS = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    public static byte[] serialize(Object object) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (Output output = new Output(os)) {
            Kryo kryo = KRYOS.get();
            kryo.writeObjectOrNull(output, object, object.getClass());
            output.flush();
            return os.toByteArray();
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try (Input input = new Input(in)) {
            Kryo kryo = KRYOS.get();
            return kryo.readObjectOrNull(input, clazz);
        }
    }
}
