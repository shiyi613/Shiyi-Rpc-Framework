package com.shiyi.remoting.transport.socket;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import com.shiyi.exception.RpcException;
import com.shiyi.extension.ExtensionLoader;
import com.shiyi.registry.ServiceDiscovery;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 Socket 传输 RpcRequest
 *
 * @Author:shiyi
 * @create: 2023-05-18  13:50
 */
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {

    private final RpcProperties rpcConfig;
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        //TODO
        this.rpcConfig = RpcConfiguration.getRpcConfig();
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(rpcConfig.getRegister());

    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            log.info("client connect server[{}] successfully",inetSocketAddress.toString());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // Send data to the server through the output stream
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // Read RpcResponse from the input stream
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败",e);
        }
    }
}
