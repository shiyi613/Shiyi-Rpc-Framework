package com.shiyi.annotation;

import com.shiyi.spring.CustomConfigRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * import config register, which will generate rpc configuration bean definition
 *
 * @Author:shiyi
 * @create: 2023-05-24  16:39
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(CustomConfigRegistrar.class)
public @interface EnableConfig {
}
