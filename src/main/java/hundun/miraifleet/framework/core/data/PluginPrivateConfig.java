package hundun.miraifleet.framework.core.data;
/**
 * @author hundun
 * Created on 2021/08/10
 */

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class PluginPrivateConfig {
    Map<String, BotPrivateConfig> botConfigs = new HashMap<>(0);
}
