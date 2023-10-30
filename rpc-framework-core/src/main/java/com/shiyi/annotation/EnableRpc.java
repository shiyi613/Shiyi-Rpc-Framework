package com.shiyi.annotation;

import com.shiyi.spring.CustomScannerRegistrar;
import com.shiyi.spring.RpcApplicationListenerRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * enable rpc config properties and start {@link RpcService} annotation scan
 *
 * @Author:shiyi
 * @create: 2023-05-24  16:29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ComponentScan("com.shiyi")
@EnableConfig
@RpcScan
public @interface EnableRpc {
}
