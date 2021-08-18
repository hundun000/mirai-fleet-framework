package hundun.miraifleet.framework.starter.botlogic.function.weibo.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/04/23
 */
@Data
public class WeiboCardCache {

    String itemid;
    
    String uid;
    String screenName;
    
    String blogId;

    LocalDateTime blogCreatedDateTime;
    String blogText;
    String blogTextDetail;
    
    
    List<String> picsLargeUrls;
    
    //File singlePicture;
}
