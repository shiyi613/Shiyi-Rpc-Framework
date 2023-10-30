package com.shiyi.loadbalance.loadbalancer;

import com.shiyi.loadbalance.AbstractLoadBalance;
import com.shiyi.registry.zk.util.CuratorUtils;
import com.shiyi.remoting.dto.RpcRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 平滑加权轮询
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private static final int RECYCLE_PERIOD = 60000;

    protected static class WeightedRoundRobin{
        private int weight;
        private AtomicLong current = new AtomicLong(0);
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        public long increaseCurrent() {
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

    private ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>();


    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {

        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.computeIfAbsent(rpcServiceName, k -> new ConcurrentHashMap<>());
        int totalWeight = 0;
        long maxCurrent = Integer.MIN_VALUE;
        long now = System.currentTimeMillis();
        String selectedAdd = null;
        WeightedRoundRobin selectedWRR = null;
        for (String serviceAddress : serviceAddresses) {
            int weight = CuratorUtils.getWeight(rpcServiceName, serviceAddress);
            WeightedRoundRobin weightedRoundRobin = map.computeIfAbsent(serviceAddress, k -> {
                WeightedRoundRobin wrr = new WeightedRoundRobin();
                wrr.setWeight(weight);
                return wrr;
            });

            if(weight != weightedRoundRobin.getWeight()){
                // weight changed
                weightedRoundRobin.setWeight(weight);
            }
            // currentWeight += weight
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            // getMaxCurrentWeight
            if(cur > maxCurrent){
                maxCurrent = cur;
                selectedAdd = serviceAddress;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if(serviceAddresses.size() != map.size()){
            map.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
        }
        if(selectedAdd != null){
            // MAX(currentWeight) -= totalWeight
            selectedWRR.sel(totalWeight);
            return selectedAdd;
        }
        return serviceAddresses.get(0);
    }
}
