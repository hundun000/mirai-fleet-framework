package hundun.miraifleet.framework.starter.botlogic.function.reminder.db;

import java.io.File;
import hundun.miraifleet.framework.core.helper.repository.MapDocumentRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderList;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/13
 */
public class ReminderListRepository extends MapDocumentRepository<ReminderList> {

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
