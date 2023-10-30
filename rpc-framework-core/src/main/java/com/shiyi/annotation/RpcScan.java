package com.shiyi.annotation;

import com.shiyi.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/**
 * scan custom annotations
 *
 * @Author:shiyi
 * @create: 2023-05-22  21:01
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {

    String[] basePackage() default {};
}
