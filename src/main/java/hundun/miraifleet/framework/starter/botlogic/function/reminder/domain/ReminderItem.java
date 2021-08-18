package hundun.miraifleet.framework.starter.botlogic.function.reminder.domain;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@Data
public class ReminderItem {
    String id;
    int count;
    long targetGroup;
    String text;
    int monthCondition;
    int dayOfMonthCondition;
    int dayOfWeekCondition;
    int hourCondition;
    int minuteCondition;
}
