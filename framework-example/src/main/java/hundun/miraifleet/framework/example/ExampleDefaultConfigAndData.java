package hundun.miraifleet.framework.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboConfig;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboPushFilterFlag;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;

/**
 * @author hundun
 * Created on 2021/12/20
 */
public class ExampleDefaultConfigAndData {

    static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>(1);
        map.put(k1, v1);
        return map;
    }

    static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>(2);
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * 样例内容：<br>
     * 订阅：明日方舟Arknights（uid:6279793937），
     * 模式：ALL_IMAGE，
     * 过滤：RETWEET
     * @return
     */
    public static Supplier<WeiboConfig> weiboConfigDefaultDataSupplier() {
        return () -> {
            WeiboConfig weiboConfig = new WeiboConfig(
                    mapOf("6279793937", WeiboViewFormat.ALL_IMAGE),
                    mapOf("6279793937", Arrays.asList(WeiboPushFilterFlag.RETWEET)));
            return weiboConfig;
        };
    }
}
