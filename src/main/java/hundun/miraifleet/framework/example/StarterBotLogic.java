package hundun.miraifleet.framework.example;

import java.util.ArrayList;
import java.util.List;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.starter.botlogic.function.MiraiCodeFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;

/**
 * @author hundun
 * Created on 2021/08/06
 */
public class StarterBotLogic extends BaseBotLogic {

    
    MiraiCodeFunction miraiCodeFunction;
    
    RepeatFunction repeatFunction;

    
    public StarterBotLogic(JvmPlugin plugin) {
        super(plugin, "Starter");
        
        miraiCodeFunction = new MiraiCodeFunction(this, plugin, characterName);
        functions.add(miraiCodeFunction);
        
        repeatFunction = new RepeatFunction(this, plugin, characterName);
        functions.add(miraiCodeFunction);
        
    }
    

    
}
