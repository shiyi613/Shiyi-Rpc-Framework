package com.shiyi.annotation;

import java.lang.annotation.*;

/**
 * RPC service annotation, marked on the service implementation class
 *
 * @Author:shiyi
 * @create: 2023-05-22  17:51
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

    /**
     * server timeout(ms)
     */
    String timeout() default "";

    /**
     * async
     */
    boolean async() default false;
}
