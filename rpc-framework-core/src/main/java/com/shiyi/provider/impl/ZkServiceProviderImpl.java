package com.shiyi.provider.impl;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.constants.RpcTimeoutConstant;
import com.shiyi.enums.RpcErrorMessageEnum;
import com.shiyi.exception.RpcException;
import com.shiyi.extension.ExtensionLoader;
import com.shiyi.provider.ServiceProvider;
import com.shiyi.registry.ServiceRegistry;
import com.shiyi.remoting.transport.netty.server.NettyRpcServer;
import com.shiyi.utils.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author:shiyi
 * @create: 2023-05-18  18:02
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private static final String DEFAULT_SERVER_TIMEOUT = RpcTimeoutConstant.DEFAULT_SERVER_TIMEOUT;

    private final Map<String,Object> serviceMap;
    private final Map<String,String> timeoutMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        //TODO
        serviceMap = new ConcurrentHashMap<>();
        timeoutMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(RpcConfiguration.getRpcConfig().getRegister());
    }


    @SneakyThrows
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getClazz().newInstance());
        timeoutMap.compute(rpcServiceName, (s,u) ->
                StringUtil.isBlank(rpcServiceConfig.getTimeout()) ? DEFAULT_SERVER_TIMEOUT : rpcServiceConfig.getTimeout()
        );
        log.info("Add service:{} and interfaces:{}",rpcServiceName,rpcServiceConfig.getClazz().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public String getServiceTimeout(String rpcServiceName) {
        String timeout = timeoutMap.get(rpcServiceName);
        if(StringUtil.isBlank(timeout)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return timeout;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registryService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
