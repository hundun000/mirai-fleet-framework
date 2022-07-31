package hundun.miraifleet.framework.starter.botlogic.function.reminder.data;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReminderItem {
    Integer count;
    @Deprecated
    String text;
    List<String> reminderMessageCodes;
    String cron;
    
    
    
    public static class Factory {

        public static ReminderItem createByHourlyChatCron(
                int hourCondition,
                String text
                ) {
            String cron = "* 0 " + hourCondition + " * * ?";
            return create(cron, text, null);
        }

        public static ReminderItem create(String text, String cron) {
            return create(cron, text, null);
        }

        public static ReminderItem create(
                String cornRawFomat,
                String text,
                Integer count
                ) {
            ReminderItem task = new ReminderItem();
            String cron = cornRawFomat.replace("~", " ");
            task.setCron(cron);
            task.setCount(count);
            task.setReminderMessageCodes(Arrays.asList(text));
            return task;
        }
    }
}
