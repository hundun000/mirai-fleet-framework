package hundun.miraifleet.framework.starter.botlogic.function.weibo.db;


import java.io.File;
import java.util.function.Function;

import hundun.miraifleet.framework.core.helper.repository.PluginDataRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboUserInfoCache;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;


/**
 * @author hundun
 * Created on 2019/12/08
 */
public class WeiboUserInfoCacheRepository extends PluginDataRepository<WeiboUserInfoCache> {
    
    public WeiboUserInfoCacheRepository(
            JvmPlugin plugin,
            File file
            ) {
        super(
                plugin, 
                file,
                WeiboUserInfoCache.class, 
                (item -> item.getUid()), 
                ((item, id) -> item.setUid(id))
                );
    }

    public WeiboUserInfoCache findOneByScreenName(String screenName) {
        return findOneByFilter((item -> item.getScreenName()), screenName); 
    }

    
}
