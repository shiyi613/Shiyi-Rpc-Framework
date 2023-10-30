package com.shiyi.remoting.transport;

import com.shiyi.extension.SPI;
import com.shiyi.remoting.dto.RpcRequest;


/**
 * send RpcRequest
 *
 * @Author:shiyi
 * @create: 2023-05-18  13:46
 */
@SPI
public interface RpcRequestTransport {

    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
