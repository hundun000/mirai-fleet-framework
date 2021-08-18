package hundun.miraifleet.framework.starter.botlogic.function.reminder.db;

import java.io.File;
import java.util.function.Function;

import hundun.miraifleet.framework.core.helper.repository.PluginDataRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderList;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.TopCardInfo;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/13
 */
public class ReminderListRepository extends PluginDataRepository<ReminderList> {

    public ReminderListRepository(
            JvmPlugin plugin,
            File file
            ) {
        super(
                plugin, 
                file,
                ReminderList.class, 
                (item -> item.getBotId()), 
                ((item, id) -> item.setBotId(id))
                );
    }

}
