package com.shiyi.spring;

import com.shiyi.annotation.RpcScan;
import com.shiyi.annotation.RpcService;
import com.shiyi.config.RpcServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;

/**
 * scan {@link RpcScan} to get basePackage value and scan {@link RpcService} to register bean definitions
 *
 * @Author:shiyi
 * @create: 2023-05-22  21:04
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        if (!annotationMetadata.hasAnnotation(RpcScan.class.getName())) {
            return;
        }
        //get the attributes and values ​​of RpcScan annotation
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // Scan the RpcService annotation
        // useDefaultFilters = false,即第二个参数 表示不扫描 @Component、@ManagedBean、@Named 注解标注的类
        ClassPathBeanDefinitionScanner rpcServiceScanner = new ClassPathBeanDefinitionScanner(registry, false);
        // add our custom annotation scan
        rpcServiceScanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
        }
        // scan the packages, and register RpcServiceConfig BeanDefinition
        for (String needScanPackage : rpcScanBasePackages) {
            Set<BeanDefinition> candidateComponents = rpcServiceScanner.findCandidateComponents(needScanPackage);
            try {
                // register class which be annotated with @RpsService bean definition
                registerCandidateComponents(registry, candidateComponents);
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void registerCandidateComponents(BeanDefinitionRegistry registry, Set<BeanDefinition> candidateComponents) throws ClassNotFoundException {
        for (BeanDefinition candidateComponent : candidateComponents) {
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                Map<String, Object> annotationAttributesMap = annotationMetadata.getAnnotationAttributes(RpcService.class.getName());
                AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(annotationAttributesMap);
                String group = annotationAttributes.getString("group");
                String version = annotationAttributes.getString("version");
                String timeout = annotationAttributes.getString("timeout");
                Class<?> clazzName = Class.forName(annotationMetadata.getClassName());
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcServiceConfig.class);
                AbstractBeanDefinition beanDefinition = builder.addPropertyValue("group", group)
                        .addPropertyValue("version", version)
                        .addPropertyValue("clazz", clazzName)
                        .addPropertyValue("timeout", timeout)
                        .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                        .setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
                        .getBeanDefinition();
                registry.registerBeanDefinition(clazzName.getSimpleName(), beanDefinition);
            }
        }
    }
}
