package hundun.miraifleet.framework.helper;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mamoe.mirai.utils.MiraiLogger;



/**
 * @author hundun
 * Created on 2021/06/21
 */
public class Utils {
    
    public static String checkFolder(String subFolerName, String parentFoler) {

        String subFolerPathName = parentFoler + File.separator + subFolerName;
        File subFoler = new File(subFolerPathName);
        if (!subFoler.exists()){
            subFoler.mkdirs();
        }

        return subFolerPathName;
    }

    static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseByObjectMapper(File settingsFile, Class<T> clazz, MiraiLogger miraiLogger) {
        T result;
        try {
            result = objectMapper.readValue(settingsFile, clazz);
        } catch (IOException e) {
            miraiLogger.error(e);
            result = null;
        }
        return result;
    }



}
