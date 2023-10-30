package com.shiyi.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @Author:shiyi
 * @create: 2023-05-17  18:21
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -1871291762229514891L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;
    private String timeout;

    public String getRpcServiceName(){
        return this.getInterfaceName() + this.getGroup() + this.version;
    }

}
