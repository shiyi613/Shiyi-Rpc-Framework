package com.shiyi.remoting.transport.netty.client;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import com.shiyi.enums.CompressTypeEnum;
import com.shiyi.enums.SerializationTypeEnum;
import com.shiyi.exception.RpcException;
import com.shiyi.extension.ExtensionLoader;
import com.shiyi.factory.SingletonFactory;
import com.shiyi.registry.ServiceDiscovery;
import com.shiyi.remoting.constants.RpcConstants;
import com.shiyi.remoting.dto.RpcMessage;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;
import com.shiyi.remoting.transport.RpcRequestTransport;
import com.shiyi.remoting.transport.netty.codec.RpcMessageDecoder;
import com.shiyi.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author:shiyi
 * @create: 2023-05-21  22:11
 */
@Slf4j
@Component
public final class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final UnprocessedRequests unprocessedRequests;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final RpcProperties rpcConfig;


    public NettyRpcClient() {
        // initialize resources such as EventLoopGroup, Bootstrap TODO
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // The timeout period of the connection
                // If this time is exceeded or the connection can't be established, the connection fails
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // If no data is sent to the server within 5s, a heartbeat request is sent
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.rpcConfig = RpcConfiguration.getRpcConfig();
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(rpcConfig.getRegister());
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);

    }


    @Override
    public ResultFuture sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        ResultFuture resultFuture = new ResultFuture(rpcRequest, rpcRequest.getTimeout());
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // get server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // put unprocessed request
            resultFuture.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    //TODO
                    .codec(SerializationTypeEnum.getCode(rpcConfig.getSerializer()))
                    .compress(CompressTypeEnum.getCode(rpcConfig.getCompressName()))
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                    ResultFuture.sent(rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("send failed:", future.cause());
                }
            });
        } else {
            throw new RpcException("client send Request failed");
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successfully!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new RuntimeException("client connect " + inetSocketAddress.toString() + " error!");
            }
        });
        return completableFuture.get();
    }
}
