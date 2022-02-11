package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.starter.botlogic.function.CharacterHelpFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.ReminderFunction;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboFunction;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/06
 */
public class ExampleBotLogic extends BaseBotLogic {

    WeiboFunction weiboFunction;
    ReminderFunction reminderFunction;
    RepeatFunction repeatFunction;
    CharacterHelpFunction characterHelpFunction;
    
    public ExampleBotLogic(JvmPlugin plugin) {
        super(plugin, "framework样例");

        weiboFunction = new WeiboFunction(this, plugin, characterName, 
                false,
                ExampleDefaultConfigAndData.weiboConfigDefaultDataSupplier());
        functions.add(weiboFunction);

        reminderFunction = new ReminderFunction(this, plugin, characterName, 
                true,
                null,
                ExampleDefaultConfigAndData.hourlyChatConfigDefaultDataSupplier());
        functions.add(reminderFunction);

        repeatFunction = new RepeatFunction(this, plugin, characterName);
        functions.add(repeatFunction);
        
        characterHelpFunction = new CharacterHelpFunction(this, plugin, characterName);
        functions.add(characterHelpFunction);
        
        allCompositeCommandProxy = new ExamleAllCompositeCommandProxy(this, plugin, characterName);
    }

}
