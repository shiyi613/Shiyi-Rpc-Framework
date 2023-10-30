package com.shiyi.remoting.transport.netty.server;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import com.shiyi.constants.RpcTimeoutConstant;
import com.shiyi.enums.CompressTypeEnum;
import com.shiyi.enums.RpcResponseCodeEnum;
import com.shiyi.enums.SerializationTypeEnum;
import com.shiyi.factory.SingletonFactory;
import com.shiyi.handler.RpcRequestHandler;
import com.shiyi.provider.ServiceProvider;
import com.shiyi.provider.impl.ZkServiceProviderImpl;
import com.shiyi.remoting.constants.RpcConstants;
import com.shiyi.remoting.dto.RpcMessage;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 *
 * @Author:shiyi
 * @create: 2023-05-22  12:06
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcProperties rpcConfig;
    private final RpcRequestHandler rpcRequestHandler;
    private final ServiceProvider serviceProvider;

    public NettyRpcServerHandler() {
        this.rpcConfig = RpcConfiguration.getRpcConfig();
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                CompletableFuture.runAsync(() -> {
                    log.info("server receive message: [{}],thread:[{}]", msg, Thread.currentThread().getName());
                    byte messageType = ((RpcMessage) msg).getMessageType();
                    RpcMessage rpcMessage = new RpcMessage();
                    // TODO
                    rpcMessage.setCodec(SerializationTypeEnum.getCode(rpcConfig.getSerializer()));
                    rpcMessage.setCompress(CompressTypeEnum.getCode(rpcConfig.getCompressName()));
                    if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                        rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                        rpcMessage.setData(RpcConstants.PONG);
                    } else if (messageType == RpcConstants.REQUEST_TYPE) {
                        RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                        // Execute the target method (the client needs to execute) and return the method result
                        Object result = rpcRequestHandler.handle(rpcRequest);
                        log.info(String.format("server get result: %s", result == null ? null : result.toString()));
                        rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                        if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                            RpcResponse<Object> rpcResponse;
                            if (RpcTimeoutConstant.TIMEOUT_RESPONSE.equals(result)) {
                                rpcResponse = RpcResponse.fail(rpcRequest.getRequestId(), RpcResponseCodeEnum.SERVER_TIMEOUT);
                            } else {
                                rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                            }
                            rpcMessage.setData(rpcResponse);
                        } else {
                            RpcResponse<Object> rpcResponse = RpcResponse.fail(rpcRequest.getRequestId(), RpcResponseCodeEnum.FAIL);
                            rpcMessage.setData(rpcResponse);
                            log.error("not writeable now, message dropped");
                        }
                    }
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                });
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * no client message received for 30 seconds, so close the connection between the client and the server
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check(30s read idle) happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
