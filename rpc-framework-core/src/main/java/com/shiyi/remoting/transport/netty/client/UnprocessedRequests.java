package com.shiyi.remoting.transport.netty.client;

import com.shiyi.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * store bindings between unprocessed request by the server and the corresponding response
 *
 * @Author:shiyi
 * @create: 2023-05-22  10:34
 */
@Slf4j
public class UnprocessedRequests {

    private static CompletableFuture<RpcResponse<Object>> lastFuture;

    private static final Map<String, CompletableFuture<RpcResponse<Object>>> FUTURES = new ConcurrentHashMap<>();

    public static CompletableFuture<RpcResponse<Object>> getFuture(){
        if(lastFuture == null){
            return new CompletableFuture<>();
        }
        return lastFuture;
    }

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        lastFuture = future;
        FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }



}
