package com.shiyi.annotation;

import com.sun.javafx.image.IntPixelGetter;

import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 *
 * @Author:shiyi
 * @create: 2023-05-22  21:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

    /**
     * timeout(ms)
     */
    String timeout() default "";

    /**
     * Async
     */
    boolean async() default false;

}
