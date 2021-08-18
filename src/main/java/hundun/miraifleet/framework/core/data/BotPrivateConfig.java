package hundun.miraifleet.framework.core.data;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@Data
public class BotPrivateConfig {
    Map<String, FunctionPrivateConfig> functionConfigs = new HashMap<>(1);
}
