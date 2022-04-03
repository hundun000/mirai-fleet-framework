package hundun.miraifleet.framework.starter.botlogic.function.reminder.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReminderItem {
    Integer count;
    String text;
    String cron;
}
