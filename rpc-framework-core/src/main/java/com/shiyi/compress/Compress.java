package com.shiyi.compress;

import com.shiyi.extension.SPI;

/**
 * @Author:shiyi
 * @create: 2023-05-21  1:02
 */
@SPI
public interface Compress {

    /**
     * 压缩
     * @param bytes 字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes 字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
