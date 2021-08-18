package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.starter.botlogic.function.MiraiCodeFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.ReminderFunction;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboFunction;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/06
 */
public class AmiyaExampleBotLogic extends BaseBotLogic {

    WeiboFunction weiboFunction;
    ReminderFunction reminderFunction;
    
    public AmiyaExampleBotLogic(JvmPlugin plugin) {
        super(plugin, "阿米娅");
        
        weiboFunction = new WeiboFunction(this, plugin, characterName);
        functions.add(weiboFunction);
        
        reminderFunction = new ReminderFunction(this, plugin, characterName);
        functions.add(reminderFunction);
    }
    
}
