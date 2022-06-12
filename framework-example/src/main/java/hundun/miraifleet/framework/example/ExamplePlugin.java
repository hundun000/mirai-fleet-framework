package hundun.miraifleet.framework.example;

import org.jetbrains.annotations.NotNull;

import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public class ExamplePlugin extends JavaPlugin {

    public static final ExamplePlugin INSTANCE = new ExamplePlugin();

    ExampleBotLogic botLogic;

    public ExamplePlugin() {
        super(new JvmPluginDescriptionBuilder(
                "hundun.fleet.framework-example",
                "0.1.0"
            )
            .build());
    }

    @Override
    public void onEnable() {
        botLogic = new ExampleBotLogic(this);
        botLogic.onBotLogicEnable();
    }

    @Override
    public void onDisable() {

        botLogic.onDisable();
        // 由GC回收即可
        botLogic = null;
    }

}
