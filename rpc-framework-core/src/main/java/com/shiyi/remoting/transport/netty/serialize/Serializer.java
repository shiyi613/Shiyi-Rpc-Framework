package com.shiyi.remoting.transport.netty.serialize;

/**
 * @Author:shiyi
 * @create: 2023-05-17  18:51
 */
public interface Serializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
