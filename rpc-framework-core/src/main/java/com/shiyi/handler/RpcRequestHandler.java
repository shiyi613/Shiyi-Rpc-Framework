package com.shiyi.handler;

import com.shiyi.constants.RpcTimeoutConstant;
import com.shiyi.exception.RpcException;
import com.shiyi.factory.SingletonFactory;
import com.shiyi.provider.ServiceProvider;
import com.shiyi.provider.impl.ZkServiceProviderImpl;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.utils.StringUtil;
import com.shiyi.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;


/**
 * RpcRequest processor
 *
 * @Author:shiyi
 * @create: 2023-05-18  23:38
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    private final ExecutorService executor;

    public RpcRequestHandler(){
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        executor = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("server");
    }

    /**
     * Processing rpcRequest: call the corresponding method and then return the result
     * @return method invoke result
     */
    public Object handle(RpcRequest rpcRequest){
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // get timeout，client timeout priority > server timeout priority
        String timeout = "";
        String serverTimeout = serviceProvider.getServiceTimeout(rpcServiceName);
        String clientTimeout = rpcRequest.getTimeout();
        // client timeout is null or default
        if(StringUtil.isBlank(clientTimeout) || RpcTimeoutConstant.DEFAULT_CLIENT_TIMEOUT.equals(clientTimeout)){
            timeout = serverTimeout;
        }else{
            timeout = clientTimeout;
        }
        Object service = serviceProvider.getService(rpcServiceName);
        Object result = null;
        CompletableFuture<Object> resultFuture = CompletableFuture.supplyAsync(() -> invokeTargetMethod(rpcRequest, service), executor);
        try {
             result = resultFuture.get(Long.parseLong(timeout), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("RpcRequest [{}] 处理异常/超时，设定的超时时间为 {}", rpcRequest, timeout);
            result = RpcTimeoutConstant.TIMEOUT_RESPONSE;
        }
        return result;
    }

    /**
     * get method execution results
     *
     * @param rpcRequest rpc request
     * @param service target service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
