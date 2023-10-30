package com.shiyi.loadbalance.loadbalancer;

import com.shiyi.loadbalance.AbstractLoadBalance;
import com.shiyi.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性哈希算法
 * refer to dubbo consistent hash load balance: https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 *
 * @Author:shiyi
 * @create: 2023-05-26  10:53
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    /**
     * 缓存，一个方法对应一个选择器，当节点增加或者减少都会刷新哈希环
     */
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    /**
     * 虚拟结点数，避免哈希环倾斜（数据倾斜）
     * 数据倾斜：当节点数较少时，节点分布不均匀，会导致大部分请求落在同在节点上
     */
    private static final int replicaNumber = 160;

    /**
     * @param serviceAddresses 方法实现类所在节点的所有地址
     * @param rpcRequest 方法调用参数实体类
     * @return String 负载均衡选择的一个调用地址（IP + PORT)
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // get hash code
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates，当服务提供者增加/减少时会更新
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, replicaNumber, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        // interfaceName + all parameterValue as key
        return selector.select(rpcRequest);
    }


    // 一致性哈希选择器，一个方法对应一个选择器
    static class ConsistentHashSelector {
        /**
         * 哈希环：一个方法所有实例地址，每个实例地址对应 replicaNumber 个虚拟结点，这是保存所有虚拟结点的容器
         */
        private final TreeMap<Long, String> virtualInvokers;

        // 方法提供者的所有地址对应的哈希值
        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 对 address + i 进行 md5 运算，得到一个长度为16的字节数组
                    byte[] digest = md5(invoker + i);
                    // 对 digest 部分字节进行4次 hash 运算，得到四个不同的 long 型正整数
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        // 作为虚拟结点的key
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        public String select(RpcRequest rpcRequest) {
            String key = toKey(rpcRequest);
            byte[] digest = md5(key);
            return selectForKey(hash(digest, 0));
        }

        // 负载均衡的结果跟所有方法参数值有关，相同的参数参数值，负载均衡结果一样
        private String toKey(RpcRequest request){
            StringBuffer buf = new StringBuffer();
            buf.append(request.getRpcServiceName());
            Object[] parameters = request.getParameters();
            for (Object parameter : parameters) {
                buf.append(parameter);
            }
            return buf.toString();
        }

        public String selectForKey(long hashCode) {
            // tailMap: 获取一个子集。其所有对象的 key 的值大于等于 fromKey
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            // 没有比hashCode大的，说明位于哈希环末尾，这时候选择第一个节点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        // Ketama算法: 这是基于MD5的散列函数
        static long hash(byte[] digest, int idx) {
            return  ( (long) (digest[3 + idx * 4] & 0xFF) << 24
                    | (long) (digest[2 + idx * 4] & 0xFF) << 16
                    | (long) (digest[1 + idx * 4] & 0xFF) << 8
                    | (long) (digest[0 + idx * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }




    }

}
