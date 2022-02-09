package hundun.miraifleet.framework.starter.botlogic.function.weibo.feign;


import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import hundun.miraifleet.framework.core.helper.feign.FeignClientFactory;
import net.mamoe.mirai.utils.MiraiLogger;






//@FeignClient(
//        name = "weiboPictureApiService",
//        url = "https://wx2.sinaimg.cn",
//        configuration = PublicFeignConfiguration.class
//)
public interface WeiboPictureApiFeignClient {

    public static WeiboPictureApiFeignClient instance(MiraiLogger miraiLogger) {
        return FeignClientFactory.get(WeiboPictureApiFeignClient.class, "https://wx2.sinaimg.cn", miraiLogger);
    }

    @RequestLine("GET /large/{id}")
    @Headers({
        "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1"
        })
    Response pictures(@Param("id") String id);
}
