package com.shiyi.remoting.transport.netty.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.shiyi.exception.SerializeException;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author:shiyi
 * @create: 2023-05-17  18:43
 */
public class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo>  kryoThreadLocal = ThreadLocal.withInitial(()->{
         Kryo kryo = new Kryo();
         kryo.register(RpcRequest.class);
         kryo.register(RpcResponse.class);
         kryo.setReferences(true);
         kryo.setRegistrationRequired(false);
         return kryo;
    });


    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output,obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }
}
