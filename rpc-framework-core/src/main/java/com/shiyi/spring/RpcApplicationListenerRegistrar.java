package com.shiyi.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * rpc {@link org.springframework.context.ApplicationListener ApplicationListeners} Registrar
 *
 * @Author:shiyi
 * @create: 2023-05-23  11:38
 */
@Component
public class RpcApplicationListenerRegistrar implements ApplicationContextAware {

    /**
     * The bean name of {@link RpcApplicationListenerRegistrar}
     */
    public static final String BEAN_NAME = "rpcApplicationListenerRegistrar";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        addApplicationListeners((ConfigurableApplicationContext) applicationContext);
    }

    private void addApplicationListeners(ConfigurableApplicationContext context) {
        context.addApplicationListener(createRpcBootstrapApplicationListener(context));
    }

    private ApplicationListener<?> createRpcBootstrapApplicationListener(ConfigurableApplicationContext context) {
        return new RpcBootstrapApplicationListener(context);
    }
}
