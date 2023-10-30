package com.shiyi.spring;

import com.shiyi.config.RpcProperties;
import com.shiyi.utils.PropertiesFileUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Properties;

/**
 * read rpc.properties, and generate bean definition
 *
 * @Author:shiyi
 * @create: 2023-05-24  16:37
 */
public class CustomConfigRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 4 rpc config properties locations: classpath + one of CONFIG_PACKAGE_PATHS + CONFIG_NAME
     * Priority from low to high, If there are multiple paths, they will be overwritten according to this priority
     */
    private static final String[] CONFIG_PACKAGE_PATHS = {"properties/", "config/", "META-INF/", ""};
    private static final String CONFIG_NAME = "rpc.properties";
    /**
     * The bean name of rpc configuration properties
     */
    private static final String CONFIG_BEAN_NAME = "rpcConfig";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // read the properties
        for (String configPackagePath : CONFIG_PACKAGE_PATHS) {
            Properties properties = null;
            properties = PropertiesFileUtil.readPropertiesFile(configPackagePath + CONFIG_NAME);
            if(properties == null)continue;
            String compressName = properties.getProperty("rpc.compressName");
            String loadBalance = properties.getProperty("rpc.loadBalance");
            String register = properties.getProperty("rpc.register");
            String address = properties.getProperty("rpc.address");
            String transporter = properties.getProperty("rpc.transporter");
            String serverPort = properties.getProperty("rpc.server.port");
            String serializer = properties.getProperty("rpc.serializer");
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RpcProperties.class)
                    .addPropertyValue("compressName", compressName)
                    .addPropertyValue("loadBalance", loadBalance)
                    .addPropertyValue("register", register)
                    .addPropertyValue("address", address)
                    .addPropertyValue("transporter", transporter)
                    .addPropertyValue("serverPort", serverPort)
                    .addPropertyValue("serializer", serializer)
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                    .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                    .getBeanDefinition();
            if (registry.containsBeanDefinition(CONFIG_BEAN_NAME))
                registry.removeBeanDefinition(CONFIG_BEAN_NAME);
            registry.registerBeanDefinition(CONFIG_BEAN_NAME, beanDefinition);
        }
    }
}
