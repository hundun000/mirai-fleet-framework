package hundun.miraifleet.framework.starter.botlogic.function.reminder.domain;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@Data
public class ReminderItem {

    Integer count;
    String text;
    String cron;
}
