package hundun.miraifleet.framework.core.botlogic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hundun.miraifleet.framework.core.data.FunctionPrivateConfig;
import hundun.miraifleet.framework.core.data.PluginPrivateConfig;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.helper.Utils;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;

/**
 * @author hundun
 * Created on 2021/08/09
 */
public abstract class BaseBotLogic {
    public static final String RESOURCE_DOWNLOAD_FOLDER = "file_cache";
    protected final String characterName;
    protected final JvmPlugin plugin;
    
    protected List<BaseFunction<?>> functions = new ArrayList<>();
    protected PluginPrivateConfig pluginPrivateConfig;
    
    public BaseBotLogic(JvmPlugin plugin, String characterName) {
        super();
        this.characterName = characterName;
        this.plugin = plugin;
    }
    
    public void onBotLogicEnable() {
        
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);
        
//        StringBuilder commands = new StringBuilder();
        StringBuilder listenerHosts = new StringBuilder();
        
        for (BaseFunction<?> function : functions) {
//            if (function.asCompositeCommand) {
//                CommandManager.INSTANCE.registerCommand(function, false);
//                commands.append(function.toString()).append(",");
//            }
            if (function.asListenerHost) {
                eventChannel.registerListenerHost(function);
                listenerHosts.append(function.toString()).append(",");
            }
        }
        
//        plugin.getLogger().info("has commands: " + commands.toString());
        plugin.getLogger().info("has listenerHosts: " + listenerHosts.toString());
     
        
        File settingsFile = plugin.resolveConfigFile("pluginPrivateConfig.json");
        pluginPrivateConfig = Utils.parseByObjectMapper(settingsFile, PluginPrivateConfig.class, plugin.getLogger());
        if (pluginPrivateConfig == null) {
            pluginPrivateConfig = new PluginPrivateConfig();
        }
    }

    public void onDisable() {
        // TODO
    }
    
//    public File resolveDataFileOfFileCache() {
//        return plugin.resolveDataFile(RESOURCE_DOWNLOAD_FOLDER);
//    }
    
    
    public boolean isDisabledContact(BaseFunction<?> function, long contactId) {
        FunctionPrivateConfig allFunctionPrivateConfig = pluginPrivateConfig.getFunctionPrivateConfigs().get("ALL");
        if (allFunctionPrivateConfig.getDisabledContactIds().contains(contactId)) {
            return true;
        }
        
        FunctionPrivateConfig functionPrivateConfig = pluginPrivateConfig.getFunctionPrivateConfigs().get(function.getFunctionName());
        if (functionPrivateConfig != null) {
             return functionPrivateConfig.getDisabledContactIds().contains(contactId);
        }
        return false;
    }
    
}
