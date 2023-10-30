package com.shiyi.spring;

import com.shiyi.config.RpcServiceConfig;
import com.shiyi.factory.SingletonFactory;
import com.shiyi.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @Author:shiyi
 * @create: 2023-05-23  12:15
 */
@Component
public class RpcBootstrapApplicationListener extends OnceApplicationContextEventListener implements Ordered{

    /**
     * The bean name of {@link RpcBootstrapApplicationListener}
     */
    private static final String BEAN_NAME = "rpcBootstrapApplicationListener";

    private final NettyRpcServer nettyRpcServer;

    public RpcBootstrapApplicationListener(){
        this.nettyRpcServer = SingletonFactory.getInstance(NettyRpcServer.class);
    }

    public RpcBootstrapApplicationListener(ApplicationContext context){
        super(context);
        this.nettyRpcServer = SingletonFactory.getInstance(NettyRpcServer.class);
    }

    @Override
    protected void onApplicationContextEvent(ApplicationContextEvent event) {
        if(event instanceof ContextRefreshedEvent){
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        // scan rpcServiceConfig beans,and expose it
        ApplicationContext applicationContext = getApplicationContext();
        String[] beanNames = applicationContext.getBeanNamesForType(RpcServiceConfig.class);
        for (String beanName : beanNames) {
            RpcServiceConfig bean = (RpcServiceConfig) applicationContext.getBean(beanName);
            nettyRpcServer.registerService(bean);
        }
        // netty server start
        nettyRpcServer.start();
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
