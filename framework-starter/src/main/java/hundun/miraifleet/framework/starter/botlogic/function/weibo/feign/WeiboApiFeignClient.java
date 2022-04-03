package hundun.miraifleet.framework.starter.botlogic.function.weibo.feign;

import com.fasterxml.jackson.databind.JsonNode;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import hundun.miraifleet.framework.starter.helper.feign.FeignClientFactory;
import net.mamoe.mirai.utils.MiraiLogger;

//@FeignClient(
//        name = "weiboApiService",
//        url = "https://m.weibo.cn",
//        configuration = PublicFeignConfiguration.class
//)
public interface WeiboApiFeignClient {

    public static WeiboApiFeignClient instance(MiraiLogger miraiLogger) {
        return FeignClientFactory.get(WeiboApiFeignClient.class, "https://m.weibo.cn", miraiLogger);
    }

    @RequestLine("GET /api/container/getIndex?uid={uid}&type={type}&value={value}&containerid={containerid}")
    @Headers({
        "Content-Type: text/plain; charset=utf-8",
        "Accept-Language: zh-CN,zh;q=0.9",
        "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1"
        })
    JsonNode get(
            @Param("uid") String uid,
            @Param("type") String type,
            @Param("value") String value,
            @Param("containerid") String containerid
            );


    @RequestLine("GET /statuses/extend?id={id}")
    @Headers({
        "Content-Type: text/plain; charset=utf-8",
        "Accept-Language: zh-CN,zh;q=0.9",
        "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1"
        })
    JsonNode blogDetail(@Param("id") String id);



}
