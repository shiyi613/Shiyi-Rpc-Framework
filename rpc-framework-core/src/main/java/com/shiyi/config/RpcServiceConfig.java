package com.shiyi.config;

import lombok.*;

/**
 * @Author:shiyi
 * @create: 2023-05-18  20:42
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * target service class
     */
    private Class<?> clazz;

    /**
     * timeout
     */
    private String timeout;

    /**
     * Async
     */
    private boolean async;

    public String getRpcServiceName(){
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName(){
//        return this.service.getClass().getInterfaces()[0].getCanonicalName();
        return this.clazz.getInterfaces()[0].getCanonicalName();
    }


}
