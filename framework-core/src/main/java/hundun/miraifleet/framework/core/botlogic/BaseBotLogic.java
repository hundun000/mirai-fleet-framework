package hundun.miraifleet.framework.core.botlogic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import hundun.miraifleet.framework.core.function.AbstractAllCompositeCommandProxy;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionRegistryConflictException;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JavaPluginScheduler;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;

/**
 * @author hundun
 * Created on 2021/08/09
 */
public abstract class BaseBotLogic {
    @Getter
    private Permission characterCosPermission;
    protected final String characterName;
    protected final JvmPlugin plugin;

    private Map<Class<?>, BaseFunction<?>> functionMap = new HashMap<>();
    protected AbstractAllCompositeCommandProxy<?> allCompositeCommandProxy;
    

    public BaseBotLogic(JvmPlugin plugin, String characterName) {
        super();
        this.characterName = characterName;
        this.plugin = plugin;
    }

    protected File resolveBotLogicConfigFile(String jsonFileName) {
        return plugin.resolveConfigFile(jsonFileName);
    }

    public void onBotLogicEnable() {

        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);

        StringBuilder commands = new StringBuilder();
        StringBuilder listenerHosts = new StringBuilder();


        for (BaseFunction<?> function : functionMap.values()) {
            Class<?> clazz = function.getClass();
            AbstractCommand command = function.provideCommand();
            if (command != null && !function.isSkipRegisterCommand()) {
                CommandManager.INSTANCE.registerCommand(command, false);
                commands.append(clazz.getSimpleName()).append(",");
            }

            if (clazz.isAnnotationPresent(AsListenerHost.class)) {
                eventChannel.registerListenerHost(function);
                listenerHosts.append(clazz.getSimpleName()).append(",");
            }
        }

        plugin.getLogger().info("has commands: " + commands.toString());
        plugin.getLogger().info("has listenerHosts: " + listenerHosts.toString());

        characterCosPermission = registerCosPermission();
        
        if (allCompositeCommandProxy != null) {
            CommandManager.INSTANCE.registerCommand(allCompositeCommandProxy, false);
            plugin.getLogger().info("has allCompositeCommandProxy");
        }
        
    }




    public void onDisable() {
        // default do nothing
    }

    public abstract JavaPluginScheduler getPluginScheduler();

    
    /**
     * function将会：
     * 1. onBotLogicEnable时，向mirai注册function.provideCommand()（若有）<br>
     * 2. onBotLogicEnable时，向mirai注册function为ListenerHost（若是）<br>
     * 3. 可被fleet框架的其他组件通过getFunction(class)获得，用于其他用途
     */
    protected <T extends BaseFunction<?>> void registerFunction(T function) {
        functionMap.put(function.getClass(), function);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BaseFunction<?>> T getFunction(Class<T> clazz) {
        if (!functionMap.containsKey(clazz)) {
            plugin.getLogger().warning("未找到Function: " + clazz.getName());
        }
        return (T) functionMap.get(clazz);
    }

    private Permission registerCosPermission() {
        PermissionId functionPermission = plugin.permissionId("temp");
        String newHost = functionPermission.getNamespace() + ".cos";
        String newName = "INSTANCE";
        Permission newParent = Permission.getRootPermission();
        PermissionId permissionId = new PermissionId(newHost, newName);
        plugin.getLogger().info("CosPermissionId = " + permissionId.toString());
        try {
            return PermissionService.Companion.getInstance().register(
                    permissionId,
                    "略",
                    newParent
                    );
        } catch (PermissionRegistryConflictException e) {
            plugin.getLogger().error(e);
            return null;
        }
    }

}
