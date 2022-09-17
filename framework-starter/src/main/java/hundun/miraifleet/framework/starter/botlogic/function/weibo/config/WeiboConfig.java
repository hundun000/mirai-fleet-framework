package hundun.miraifleet.framework.starter.botlogic.function.weibo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @author hundun
 * Created on 2021/08/12
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WeiboConfig {
    String noNewBlogMessageTemplate;
    String newBlogMessageTemplate;
    String summaryBlogMessageTemplate;
    Map<String, WeiboViewFormat> listenConfig;
    Map<String, List<WeiboPushFilterFlag>> pushFilterFlags;
}
