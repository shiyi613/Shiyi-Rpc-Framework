import com.shiyi.HelloController;
import com.shiyi.annotation.RpcReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * @Author:shiyi
 * @create: 2023-05-22  23:58
 */
public class test {
    @Test
    public void test() throws NoSuchFieldException {
        HelloController helloController = new HelloController();
        helloController.test();
    }
}
