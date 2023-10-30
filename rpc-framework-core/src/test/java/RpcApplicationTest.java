import com.shiyi.extension.ExtensionLoader;
import com.shiyi.loadbalance.loadbalancer.ConsistentHashLoadBalance;
import com.shiyi.loadbalance.loadbalancer.RoundRobinLoadBalance;
import com.shiyi.registry.zk.util.CuratorUtils;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.remoting.dto.RpcResponse;
import com.shiyi.serialize.Serializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @Author:shiyi
 * @create: 2023-05-20  0:28
 */

public class RpcApplicationTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {


        ConsistentHashLoadBalance cb = new ConsistentHashLoadBalance();
        ArrayList<String> list = new ArrayList<String>();
        list.add("127.0.0.1:8880");
        list.add("127.0.0.1:8890");
        list.add("127.0.0.1:8990");
        RpcRequest rpcRequest = RpcRequest.builder().interfaceName("hello").build();
        for(int i = 0; i < 6; i++){
            System.out.println(cb.selectServiceAddress(list, rpcRequest));
        }

    }
}
