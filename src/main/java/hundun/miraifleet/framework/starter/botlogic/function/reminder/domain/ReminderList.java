package hundun.miraifleet.framework.starter.botlogic.function.reminder.domain;

import java.util.List;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@Data
public class ReminderList {
    String botId;
    List<ReminderItem> items;
}
