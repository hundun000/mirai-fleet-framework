package hundun.miraifleet.framework.core.configuration.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.Logger.Level;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import hundun.miraifleet.framework.core.export.IConsole;

/**
 * @author hundun
 * Created on 2021/07/01
 */
@Configuration
public abstract class BaseFeignClientConfiguration {
    
    Level feignLogLevel = Level.BASIC;
    Encoder feignEncoder = new JacksonEncoder();
    Decoder feignDecoder= new JacksonDecoder();
    
    @Autowired
    IConsole console;
    
    public <T> T get(Class<T> clazz, String url) {
        return Feign.builder()
                .encoder(feignEncoder)
                .decoder(feignDecoder)
                .logger(new MiraiFeignLogger(clazz.getSimpleName(), "debug", console.getLogger()))
                .logLevel(feignLogLevel)
                .target(clazz, url)
                ;
    }


}
