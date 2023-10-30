package com.shiyi.loadbalance;

import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.utils.CollectionUtil;

import java.util.List;

/**
 * @Author:shiyi
 * @create: 2023-05-18  16:08
 */
public abstract class AbstractLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if(CollectionUtil.isEmpty(serviceAddresses)){
            return null;
        }
        if(serviceAddresses.size() == 1){
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses,rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
