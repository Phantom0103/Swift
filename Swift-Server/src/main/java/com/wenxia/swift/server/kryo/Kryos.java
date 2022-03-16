package com.wenxia.swift.server.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.wenxia.swift.server.protocol.RpcRequest;
import com.wenxia.swift.server.protocol.RpcResponse;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author zhouw
 * @date 2022-03-16
 */
public class Kryos {

    private static final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            kryo.register(Class.class);
            kryo.register(Class[].class);
            kryo.register(Object[].class);

            UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            SynchronizedCollectionsSerializer.registerSerializers(kryo);

            return kryo;
        }
    };

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
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try (Input input = new Input(in)) {
            Kryo kryo = KRYOS.get();
            return kryo.readObjectOrNull(input, clazz);
        }
    }

}
