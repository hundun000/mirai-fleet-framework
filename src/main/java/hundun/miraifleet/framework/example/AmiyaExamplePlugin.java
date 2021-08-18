package hundun.miraifleet.framework.example;

import org.jetbrains.annotations.NotNull;

import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public class AmiyaExamplePlugin extends JavaPlugin {

    public static final AmiyaExamplePlugin INSTANCE = new AmiyaExamplePlugin(); 
    
    AmiyaExampleBotLogic botLogic;
    
    public AmiyaExamplePlugin() {
        super(new JvmPluginDescriptionBuilder(
                "hundun.fleet.example.amiya",
                "0.1.0"
            )
            .build());
    }
    
    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        
    }
    
    @Override
    public void onEnable() {
        botLogic = new AmiyaExampleBotLogic(this);
        botLogic.onBotLogicEnable();
    }
    
    @Override
    public void onDisable() {
        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
