package com.shiyi.extension;

/**
 * Helper Class for hold a value.
 *
 * @Author:shiyi
 * @create: 2023-05-19  18:28
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}
