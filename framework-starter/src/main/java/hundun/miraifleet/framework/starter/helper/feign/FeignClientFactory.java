package hundun.miraifleet.framework.starter.helper.feign;


import java.util.concurrent.TimeUnit;

import feign.Feign;
import feign.Feign.Builder;
import feign.Logger.Level;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/07/01
 */
public class FeignClientFactory {

    static Level feignLogLevel = Level.BASIC;
    static Encoder feignEncoder = new JacksonEncoder();
    static Decoder feignDecoder= new JacksonDecoder();


    public static <T> T get(Class<T> clazz, String url, MiraiLogger miraiLogger) {
        return getBaseBuilder()
                .logger(new MiraiFeignLogger(clazz.getSimpleName(), "debug", miraiLogger))
                .options(new Request.Options(10, TimeUnit.SECONDS, 20, TimeUnit.SECONDS, true))
                .target(clazz, url)
                ;
    }

    public static <T> Builder getBaseBuilder() {
        return Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logLevel(feignLogLevel)
                ;
    }
    
}
