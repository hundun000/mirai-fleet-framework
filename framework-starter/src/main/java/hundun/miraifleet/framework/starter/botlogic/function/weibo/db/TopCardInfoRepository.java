package hundun.miraifleet.framework.starter.botlogic.function.weibo.db;



import java.io.File;
import java.util.HashMap;

import hundun.miraifleet.framework.helper.repository.MapDocumentRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.TopCardInfo;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;


/**
 * @author hundun
 * Created on 2019/12/08
 */
public class TopCardInfoRepository extends MapDocumentRepository<TopCardInfo> {

    public TopCardInfoRepository(
            JvmPlugin plugin,
            File file
            ) {
        super(
                plugin,
                file,
                TopCardInfo.class,
                (item -> item.getUid()),
                ((item, id) -> item.setUid(id)),
                () -> new HashMap<>()
                );
    }





}
