package hundun.miraifleet.framework.example;

import org.jetbrains.annotations.NotNull;

import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public class PrinzEugenExamplePlugin extends JavaPlugin {

    public static final PrinzEugenExamplePlugin INSTANCE = new PrinzEugenExamplePlugin(); 
    
    PrinzEugenExampleBotLogic botLogic;
    
    public PrinzEugenExamplePlugin() {
        super(new JvmPluginDescriptionBuilder(
                "hundun.fleet.example.prinzeugen",
                "0.1.0"
            )
            .build());
    }
    
    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        
    }
    
    @Override
    public void onEnable() {
        botLogic = new PrinzEugenExampleBotLogic(this);
        botLogic.onBotLogicEnable();
    }
    
    @Override
    public void onDisable() {
        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
