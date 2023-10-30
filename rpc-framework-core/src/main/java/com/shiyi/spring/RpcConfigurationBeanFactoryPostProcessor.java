package com.shiyi.spring;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @Author:shiyi
 * @create: 2023-05-24  13:20
 */
@Component
public class RpcConfigurationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        RpcProperties bean = beanFactory.getBean(RpcProperties.class);
        RpcConfiguration rpcConfiguration = new RpcConfiguration(bean);
        beanFactory.registerSingleton("rpcConfiguration",rpcConfiguration);
    }
}
