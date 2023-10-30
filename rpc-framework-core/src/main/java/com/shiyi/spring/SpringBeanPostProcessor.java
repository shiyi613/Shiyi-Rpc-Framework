package com.shiyi.spring;

import com.shiyi.annotation.RpcReference;
import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.extension.ExtensionLoader;
import com.shiyi.proxy.RpcClientProxy;
import com.shiyi.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author:shiyi
 * @create: 2023-05-22  23:50
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final RpcRequestTransport rpcClient;
    private final RpcProperties rpcConfig;

    public SpringBeanPostProcessor() {
        //TODO
        this.rpcConfig = RpcConfiguration.getRpcConfig();
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(rpcConfig.getTransporter());

    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .timeout(rpcReference.timeout())
                        .async(rpcReference.async())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                declaredField.set(bean,proxy);
            }
        }
        return bean;
    }

}
