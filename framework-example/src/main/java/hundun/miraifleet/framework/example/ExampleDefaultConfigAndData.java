package hundun.miraifleet.framework.example;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboFunction;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboConfig;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboPushFilterFlag;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;

/**
 * @author hundun
 * Created on 2021/12/20
 */
public class ExampleDefaultConfigAndData {

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
                    WeiboFunction.DEFAULT_NONEWBLOGMESSAGETEMPLATE,
                    WeiboFunction.DEFAULT_NEWBLOGMESSAGETEMPLATE,
                    WeiboFunction.DEFAULT_SUMMARYBLOGMESSAGETEMPLATE,
                    Map.of("6279793937", WeiboViewFormat.ALL_IMAGE),
                    Map.of("6279793937", Arrays.asList(WeiboPushFilterFlag.RETWEET)));
            return weiboConfig;
        };
    }
}
