package hundun.miraifleet.framework;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.quartz.CronExpression;


/**
 * @author hundun
 * Created on 2021/12/14
 */
public class CronTest {
    @Test
    public void expressionTest() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日H时m分");
        
        CronExpression customExp = new CronExpression("* 1 1 ? * FRI");
        CronExpression hourlyExp = new CronExpression("* 0 * * * ?");
        
        Date date1 = dateFormat.parse("2021年1月1日1时0分");
        Date date2 = dateFormat.parse("2021年1月1日1时1分");
        Date date3 = dateFormat.parse("2021年1月2日1时1分");
        Date date4 = dateFormat.parse("2021年1月1日2时1分");
        
        assertEquals(false, customExp.isSatisfiedBy(date1));
        assertEquals(true, customExp.isSatisfiedBy(date2));
        assertEquals(false, customExp.isSatisfiedBy(date3));
        assertEquals(false, customExp.isSatisfiedBy(date4));
        
        assertEquals(true, hourlyExp.isSatisfiedBy(date1));
        assertEquals(false, hourlyExp.isSatisfiedBy(date2));
        assertEquals(false, hourlyExp.isSatisfiedBy(date3));
        assertEquals(false, hourlyExp.isSatisfiedBy(date4));
    }
}
