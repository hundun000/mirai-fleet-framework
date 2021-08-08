package hundun.miraifleet.framework.example;

import org.jetbrains.annotations.NotNull;

import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;

public class StarterPlugin extends JavaPlugin {

    public static final StarterPlugin INSTANCE = new StarterPlugin(); 
    
    StarterBotLogic botLogic;
    
    public StarterPlugin() {
        super(new JvmPluginDescriptionBuilder(
                "hundun.starter",
                "0.1.0"
            )
            .build());
    }
    
    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        
    }
    
    @Override
    public void onEnable() {
        botLogic = new StarterBotLogic(this);
        botLogic.onBotLogicEnable();
    }
    
    @Override
    public void onDisable() {
        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
