package com.shiyi.loadbalance.loadbalancer;

import com.shiyi.loadbalance.AbstractLoadBalance;
import com.shiyi.registry.zk.util.CuratorUtils;
import com.shiyi.remoting.dto.RpcRequest;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机
 *
 * @Author:shiyi
 * @create: 2023-05-18  16:12
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int length = serviceAddresses.size();
        // 标志是否每个Server都有一样的权重
        boolean sameWeight = true;
        // 用来模拟我们说的坐标轴的区间范围，下标对应了invokers中每个invoker的下标，具体保存的值则是其在X轴上的右边界
        // ex: 以前面6、4、1的数据为例，则weights的数据为：weights: [6,9,10]
        int[] weights = new int[length];
        int totalWeight = 0;

        // 构造坐标轴
        for(int i = 0; i < length; i++){
            int weight = CuratorUtils.getWeight(rpcRequest.getInterfaceName(), serviceAddresses.get(i));
            totalWeight += weight;
            weights[i] = totalWeight;
            if(sameWeight && totalWeight != weight * (i + 1)){
                sameWeight = false;
            }
        }

        // 遍历服务列表，生成一个介于0-权重总和之间的随机数，如果这个随机数落在服务对应的区间，则选中他，否则继续判断下一个
        // ex: 第一次遍历到server1，是否生成的随机数在serve1的区间[0,6)，是则选中，否则继续遍历，取到SERVER2，重复上述步骤
        if(totalWeight > 0 && !sameWeight){
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for(int i = 0; i < length; i++){
                // 左闭右开
                if(offset < weights[i]){
                    return serviceAddresses.get(i);
                }
            }
        }

        // 如果每个服务的权重都一样，那么随机选择一个服务就行
        return serviceAddresses.get(ThreadLocalRandom.current().nextInt(length));
    }
}
