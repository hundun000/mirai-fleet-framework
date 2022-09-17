package hundun.miraifleet.framework.core.botlogic;

import java.util.HashMap;
import java.util.Map;

import hundun.miraifleet.framework.core.function.AbstractAllCompositeCommandProxy;
import hundun.miraifleet.framework.core.function.AllCompositeCommandProxyConfig;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.helper.repository.SingletonDocumentRepository;
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
    /**
     * NotNull after lazyInit
     */
    @Getter
    private Permission characterCosPermission;

    /**
     * NotNull after lazyInit
     */
    @Getter
    private Permission userCommandRootPermission;
    /**
     * NotNull after lazyInit
     */
    @Getter
    private Permission adminCommandRootPermission;
    
    protected final String characterName;
    protected final JvmPlugin plugin;

    private Map<Class<?>, BaseFunction> functionMap = new HashMap<>();
    protected AbstractAllCompositeCommandProxy<?> allCompositeCommandProxy;
    protected SingletonDocumentRepository<AllCompositeCommandProxyConfig> proxyConfigRepository;

    public BaseBotLogic(JvmPlugin plugin, String characterName) {
        super();
        this.characterName = characterName;
        this.plugin = plugin;
    }
    
    public AllCompositeCommandProxyConfig getAllCompositeCommandProxyConfig() {
        return proxyConfigRepository.findSingleton();
    }
    
    private void onBotLogicEnablePre() {
        Permission commandRoot = registerAnyCommandRootPermission("command.*", "所有Command");
        characterCosPermission = registerAnyCommandRootPermission("cos.instance", "总开关（command、event、timer）");
        userCommandRootPermission = registerAnyCommandRootPermission(commandRoot, "command.user*", "所有User级别的command");
        adminCommandRootPermission = registerAnyCommandRootPermission(commandRoot, "command.admin*", "所有Admin级别的command");
        plugin.getLogger().info("已注册权限： " 
                + characterCosPermission.getId().toString() + ", "
                + userCommandRootPermission.getId().toString() + ", "
                + adminCommandRootPermission.getId().toString() + ", "
                );
        
        proxyConfigRepository = new SingletonDocumentRepository<>(
                plugin, 
                plugin.resolveConfigFile("proxyConfigRepository.json"), 
                AllCompositeCommandProxyConfig.class, 
                () -> new AllCompositeCommandProxyConfig(characterName)
                );
    }

    

    private void onBotLogicEnablePost() {
        StringBuilder commands = new StringBuilder();
        StringBuilder debugCommands = new StringBuilder();
        StringBuilder listenerHosts = new StringBuilder();
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(plugin);
        
        for (BaseFunction function : functionMap.values()) {
            Class<?> clazz = function.getClass();
            AbstractCommand command = function.provideCommand();
            if (command != null && !function.isSkipRegisterCommand()) {
                CommandManager.INSTANCE.registerCommand(command, false);
                commands.append(clazz.getSimpleName()).append(",");
            }
            AbstractCommand debugCommand = function.provideDebugCommand();
            if (debugCommand != null) {
                CommandManager.INSTANCE.registerCommand(debugCommand, false);
                debugCommands.append(clazz.getSimpleName()).append(",");
            }
            if (clazz.isAnnotationPresent(AsListenerHost.class)) {
                eventChannel.registerListenerHost(function);
                listenerHosts.append(clazz.getSimpleName()).append(",");
            }
        }

        plugin.getLogger().info("has commands: " + commands.toString());
        plugin.getLogger().info("has debuCommands: " + debugCommands.toString());
        plugin.getLogger().info("has listenerHosts: " + listenerHosts.toString());

        if (allCompositeCommandProxy != null) {
            CommandManager.INSTANCE.registerCommand(allCompositeCommandProxy, false);
            plugin.getLogger().info("has allCompositeCommandProxy");
        }
    }
    
    protected abstract void onFunctionsEnable();
    
    public final void onBotLogicEnable() {

        onBotLogicEnablePre();
        onFunctionsEnable();
        onBotLogicEnablePost();
        
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
    protected <T extends BaseFunction> void registerFunction(T function) {
        functionMap.put(function.getClass(), function);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BaseFunction> T getFunction(Class<T> clazz) {
        if (!functionMap.containsKey(clazz)) {
            plugin.getLogger().warning("未找到Function: " + clazz.getName());
        }
        return (T) functionMap.get(clazz);
    }
    
    private Permission registerAnyCommandRootPermission(String plugiBasedPermissionId, String description) {
        return registerAnyCommandRootPermission(plugin.getParentPermission(), plugiBasedPermissionId, description);
    }

    
    private Permission registerAnyCommandRootPermission(Permission parent, String plugiBasedPermissionId, String description) {
        PermissionId newPermissionId = plugin.permissionId(plugiBasedPermissionId);
        try {
            return PermissionService.getInstance().register(
                    newPermissionId,
                    description,
                    parent
                    );
        } catch (PermissionRegistryConflictException e) {
            plugin.getLogger().error(e);
            throw new RuntimeException("关键权限注册失败，本插件抛出异常放弃加载");
        }
    }
    
    
}
