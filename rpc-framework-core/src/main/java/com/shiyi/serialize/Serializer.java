package com.shiyi.serialize;

import com.shiyi.extension.SPI;

/**
 * serialize interface
 *
 * @Author:shiyi
 * @create: 2023-05-20  23:34
 */
@SPI
public interface Serializer {

    /**
     * 序列化
     * @param obj 需要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     *
     * @param bytes 字节数组
     * @param clazz 目标类
     * @param <T> 类的类型，举个例子,  {@code String.class} 的类型是 {@code Class<String>}.
     *            如果不知道类的类型的话，使用 {@code Class<?>}
     * @return 反序列化对象
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
