package hundun.miraifleet.framework.starter.botlogic.function.weibo.db;



import java.io.File;
import java.util.List;
import java.util.function.Function;

import hundun.miraifleet.framework.core.helper.repository.PluginDataRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.TopCardInfo;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboCardCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboUserInfoCache;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;


/**
 * @author hundun
 * Created on 2019/12/08
 */
public class TopCardInfoRepository extends PluginDataRepository<TopCardInfo> {

    public TopCardInfoRepository(
            JvmPlugin plugin,
            File file
            ) {
        super(
                plugin, 
                file,
                TopCardInfo.class, 
                (item -> item.getUid()), 
                ((item, id) -> item.setUid(id))
                );
    }





}
