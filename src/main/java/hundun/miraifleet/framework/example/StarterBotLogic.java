package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.starter.botlogic.function.MiraiCodeFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

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
