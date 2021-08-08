package hundun.miraifleet.framework.starter.botlogic;

import java.util.ArrayList;
import java.util.List;

import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.starter.botlogic.function.MiraiCodeFunction;
import hundun.miraifleet.framework.starter.botlogic.function.RepeatFunction;
import hundun.miraifleet.framework.starter.plugin.StarterPlugin;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;

/**
 * @author hundun
 * Created on 2021/08/06
 */
public class StarterBotLogic {
    StarterPlugin plugin;
    final String characterName = "Starter"; 
    public MiraiAdaptedApplicationContext context;
    
    MiraiCodeFunction miraiCodeFunction;
    
    RepeatFunction repeatFunction;
    
    List<BaseFunction<?>> functions = new ArrayList<>();
    
    public StarterBotLogic(StarterPlugin plugin) {
        this.plugin = plugin;
        
        
        
        context = new MiraiAdaptedApplicationContext(true);
        //context.registerBean(StarterPlugin.class, () -> plugin);
        context.refresh();
        
        plugin.getLogger().info("ApplicationContext created, has beans size = " + context.getBeanDefinitionNames().length);
        
        miraiCodeFunction = new MiraiCodeFunction(plugin, characterName);
        functions.add(miraiCodeFunction);
        
        repeatFunction = new RepeatFunction(plugin, characterName);
        functions.add(miraiCodeFunction);
        
    }
    
    
    public void onEnable() {
        
       
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);
        
        
        for (BaseFunction<?> function : functions) {
            if (function.asCompositeCommand) {
                CommandManager.INSTANCE.registerCommand(function, false);
            }
            if (function.asListenerHost) {
                eventChannel.registerListenerHost(function);
            }
        }
        
        
    }
    
    public void onDisable() {
        // TODO
    }
}
