package com.shiyi.proxy;

import com.shiyi.annotation.RpcReference;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.constants.RpcTimeoutConstant;
import com.shiyi.enums.RpcErrorMessageEnum;
import com.shiyi.enums.RpcResponseCodeEnum;
import com.shiyi.exception.RpcException;
import com.shiyi.exception.TimeOutException;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;
import com.shiyi.remoting.transport.RpcRequestTransport;
import com.shiyi.remoting.transport.netty.client.NettyRpcClient;
import com.shiyi.remoting.transport.netty.client.ResultFuture;
import com.shiyi.remoting.transport.socket.SocketRpcClient;
import com.shiyi.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.MappedByteBuffer;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is
 * like calling the local method (the intermediate process is shielded)
 *
 * @Author:shiyi
 * @create: 2023-05-19  0:18
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    private static final String DEFAULT_TIMEOUT = RpcTimeoutConstant.DEFAULT_CLIENT_TIMEOUT;

    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final RpcRequestTransport rpcRequestTransport;

    private final RpcServiceConfig rpcServiceConfig;

    private final boolean async;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport){
        this(rpcRequestTransport, new RpcServiceConfig());
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
        this.async = rpcServiceConfig.isAsync();
    }


    /**
     * get the proxy object
     */
    public <T> T getProxy(Class<T> clazz) {
        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this));
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        log.info("invoked method: [{}]", method.getName());
        // 客户端超时参数
        String timeout = StringUtil.isBlank(rpcServiceConfig.getTimeout()) ? DEFAULT_TIMEOUT : rpcServiceConfig.getTimeout();
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args == null ? new Object[0] : args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .timeout(timeout)
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient) {
            ResultFuture resultFuture = (ResultFuture) rpcRequestTransport.sendRpcRequest(rpcRequest);
            // 同步调用
            if(!async){
                try {
                    rpcResponse = resultFuture.get(Long.parseLong(timeout), TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.error("method：[{}] 调用发生异常/超时，{}", method.getName(), e);
                    rpcResponse = RpcResponse.fail(rpcRequest.getRequestId(), RpcResponseCodeEnum.CLIENT_TIMEOUT);
                }
            }else{
                // 异步调用
                return rpcResponse;
            }

        }else if(rpcRequestTransport instanceof SocketRpcClient){
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }

        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if(rpcResponse.getCode() != null && rpcResponse.getCode().equals(RpcResponseCodeEnum.SERVER_TIMEOUT.getCode())){
            throw new TimeOutException(RpcErrorMessageEnum.SERVICE_SERVER_HANDLE_TIMEOUT, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
