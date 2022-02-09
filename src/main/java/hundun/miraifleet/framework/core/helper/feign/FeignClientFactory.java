package hundun.miraifleet.framework.core.helper.feign;


import feign.Feign;
import feign.Logger.Level;
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
        return Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(new MiraiFeignLogger(clazz.getSimpleName(), "debug", miraiLogger))
                .logLevel(feignLogLevel)
                .target(clazz, url)
                ;
    }


}
