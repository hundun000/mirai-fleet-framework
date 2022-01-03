package hundun.miraifleet.framework.core.botlogic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionRegistryConflictException;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.event.events.BotOnlineEvent;

/**
 * @author hundun
 * Created on 2021/08/09
 */
public abstract class BaseBotLogic {
    private Permission characterCosPermission;
    protected final String characterName;
    protected final JvmPlugin plugin;
    
    protected List<BaseFunction<?>> functions = new ArrayList<>();
    //protected PluginPrivateConfig pluginPrivateConfig;
    
    //private final PluginConfigRepository<PluginPrivateConfig> configRepository;
    
    public BaseBotLogic(JvmPlugin plugin, String characterName) {
        super();
        this.characterName = characterName;
        this.plugin = plugin;
        //this.configRepository = new PluginConfigRepository<>(plugin, resolveBotLogicConfigFile("BasePluginPrivateConfig.json"), PluginPrivateConfig.class);
    }
    
    protected File resolveBotLogicConfigFile(String jsonFileName) {
        return plugin.resolveConfigFile(jsonFileName);
    }

    public void onBotLogicEnable() {
        
        //EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);
        
        StringBuilder commands = new StringBuilder();
        StringBuilder listenerHosts = new StringBuilder();
        
        for (BaseFunction<?> function : functions) {
            Class<?> clazz = function.getClass();
            if (clazz.isAnnotationPresent(AsCommand.class)) {
                CommandManager.INSTANCE.registerCommand(function, false);
                commands.append(clazz.getSimpleName()).append(",");
            }
        }
        plugin.getLogger().info("has commands: " + commands.toString());

        GlobalEventChannel.INSTANCE.parentScope(plugin).subscribeAlways(BotOnlineEvent.class, event -> {
            EventChannel<BotEvent> botChannel = event.getBot().getEventChannel();
            for (BaseFunction<?> function : functions) {
                Class<?> clazz = function.getClass();
                if (clazz.isAnnotationPresent(AsListenerHost.class)) {
                    botChannel.registerListenerHost(function);
                    listenerHosts.append(clazz.getSimpleName()).append(",");
                }
            }
            plugin.getLogger().info("bot: " + event.getBot().getId() + "online and gains listenerHosts: " + listenerHosts.toString());
        });
        


     
//        if (configRepository.findSingleton() == null) {
//            configRepository.saveSingleton(defaultPluginPrivateConfig());
//        }
        
        characterCosPermission = registerCosPermission();
    }
    
    


    public void onDisable() {
        // default do nothing
    }
    


    public Permission getCharacterCosPermission() {
        return characterCosPermission;
    }
    
    protected Permission registerCosPermission() {
        PermissionId functionPermission = plugin.permissionId("temp");
        String newHost = functionPermission.getNamespace() + ".cos";
        String newName = "INSTANCE";
        Permission newParent = Permission.getRootPermission();
        try {
            return PermissionService.Companion.getInstance().register(
                    new PermissionId(newHost, newName), 
                    "略", 
                    newParent
                    );
        } catch (PermissionRegistryConflictException e) {
            plugin.getLogger().error(e);
            return null;
        }
    }
    
}
