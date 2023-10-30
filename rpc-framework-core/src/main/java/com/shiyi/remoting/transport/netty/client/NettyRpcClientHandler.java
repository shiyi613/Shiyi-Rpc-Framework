package com.shiyi.remoting.transport.netty.client;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.config.RpcProperties;
import com.shiyi.enums.CompressTypeEnum;
import com.shiyi.enums.SerializationTypeEnum;
import com.shiyi.factory.SingletonFactory;
import com.shiyi.remoting.constants.RpcConstants;
import com.shiyi.remoting.dto.RpcMessage;
import com.shiyi.remoting.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * </p>
 *
 * @Author:shiyi
 * @create: 2023-05-21  22:27
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;
    private final RpcProperties rpcConfig;
    // ping times counter, only self increasing in continuous cases
    private static final AtomicInteger PING_TIMES = new AtomicInteger(0);
    // the value is 12, because heartbeat packets are sent every 5 seconds, 12 times is 1 minute
    private static final Integer PING_MAX_TIMES = 60;


    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        this.rpcConfig = RpcConfiguration.getRpcConfig();
    }

    /**
     * Read the message transmitted by the server
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive message: [{}]", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    // clear ping times counter
                    PING_TIMES.set(0);
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    ResultFuture.received(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    /**
     * send heartbeat to server, which will check if the server is alive
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                if (PING_TIMES.intValue() < PING_MAX_TIMES) {
                    int pingCount = PING_TIMES.incrementAndGet();
                    log.info("write idle happen [{}] , times [{}]", ctx.channel().remoteAddress(), pingCount);
                    Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                    RpcMessage rpcMessage = RpcMessage.builder().data(RpcConstants.PING)
                            //TODO
                            .codec(SerializationTypeEnum.getCode(rpcConfig.getSerializer()))
                            .compress(CompressTypeEnum.getCode(rpcConfig.getCompressName()))
                            .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE).build();
                    channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    // PING times exceed 12, client close the connection
                    ctx.channel().close();
                    log.error("PING times exceed 12,so client close the connection");
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
