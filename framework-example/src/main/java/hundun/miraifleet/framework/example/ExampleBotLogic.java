package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.botlogic.BaseJavaBotLogic;
import hundun.miraifleet.framework.starter.botlogic.function.CharacterHelpFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import hundun.miraifleet.framework.starter.botlogic.function.drive.DriveFunction;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.ReminderFunction;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboFunction;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;

/**
 * @author hundun
 * Created on 2021/08/06
 */
public class ExampleBotLogic extends BaseJavaBotLogic {

    
    public ExampleBotLogic(JavaPlugin plugin) {
        super(plugin, "framework样例");

        registerFunction(new WeiboFunction(this, plugin, characterName, 
                        ExampleDefaultConfigAndData.weiboConfigDefaultDataSupplier())
                );

        registerFunction(new ReminderFunction(this, plugin, characterName, 
                        null,
                        ExampleDefaultConfigAndData.hourlyChatConfigDefaultDataSupplier())
                );
        
        registerFunction(new RepeatFunction(this, plugin, characterName));

        registerFunction(new CharacterHelpFunction(this, plugin, characterName));

        registerFunction(new DriveFunction(this, plugin, characterName));
        
        allCompositeCommandProxy = new ExamleAllCompositeCommandProxy(this, plugin, characterName);
    }

}
