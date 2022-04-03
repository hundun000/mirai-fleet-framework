package hundun.miraifleet.framework.core.botlogic;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JavaPluginScheduler;


/**
 * @author hundun
 * Created on 2022/04/08
 */
public abstract class BaseJavaBotLogic extends BaseBotLogic {
    private JavaPlugin plugin;
    public BaseJavaBotLogic(JavaPlugin plugin, String characterName) {
        super(plugin, characterName);
        this.plugin = plugin;
    }
    
    @Override
    public JavaPluginScheduler getPluginScheduler() {
        return plugin.getScheduler();
    }
}
