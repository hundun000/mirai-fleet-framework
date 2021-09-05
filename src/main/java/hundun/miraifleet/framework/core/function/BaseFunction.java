package hundun.miraifleet.framework.core.function;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionRegistryConflictException;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactGroup;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactMember;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.GroupMemberEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/04/25
 * @param <T>
 */
public abstract class BaseFunction<T> extends CompositeCommand implements ListenerHost {
    
    public static final String NOT_SUPPORT_RESOURCE_PLACEHOLDER = "[该终端不支持图片或音频]";
    
    private Supplier<T> sessionDataSupplier;
    protected final JvmPlugin plugin;
    protected final MiraiLogger log;
    protected final BaseBotLogic baseBotLogic;
    protected final String functionName;
    
    Map<String, T> sessionDataMap = new HashMap<>();
    
    public BaseFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName, 
            String functionName,
            Supplier<T> sessionDataSupplier
            ) {
        super(plugin, characterName + functionName, new String[]{}, "我是" + functionName, plugin.getParentPermission(), CommandArgumentContext.EMPTY);
        this.sessionDataSupplier = sessionDataSupplier;
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.baseBotLogic = baseBotLogic;
        this.functionName = functionName;
    }
    
    protected String getSessionId(CommandSender sender) {
        String sessionId;
        if (sender instanceof MemberCommandSender) {
            sessionId = String.valueOf(((MemberCommandSender)sender).getGroup().getId());
        } else {
            sessionId = "DEFAULT";
        }
        return sessionId;
    }
    
    protected T getOrCreateSessionData() {
        String sessionId = "SINGLETON";
        return getOrCreateSessionData(sessionId);
    }
    
    protected T getOrCreateSessionData(Group group) {
        String sessionId = String.valueOf(group.getId());
        return getOrCreateSessionData(sessionId);
    }

    
    protected T getOrCreateSessionData(CommandSender sender) {
        String sessionId = getSessionId(sender);
        return getOrCreateSessionData(sessionId);
    }
    
    private T getOrCreateSessionData(String sessionId) {

        T sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = sessionDataSupplier.get();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }

    
    
    public String getFunctionName() {
        return functionName;
    }
    
    protected File resolveFunctionConfigRootFolder() {
        return plugin.resolveConfigFile(functionName);
    }
    
    protected File resolveFunctionConfigFile(String jsonFileName) {
        return plugin.resolveConfigFile(functionName + File.separator + jsonFileName);
    }
    
    protected File resolveFunctionCacheFileFolder() {
        return plugin.resolveDataFile(functionName + File.separator + "caches");
    }
    
    protected File resolveFunctionRepositoryFile(String jsonFileName) {
        return plugin.resolveDataFile(functionName + File.separator + "repositories" + File.separator + jsonFileName);
    }
    
    protected File resolveFunctionDataFile(String fileName) {
        return plugin.resolveDataFile(functionName + File.separator + fileName);
    }
    
    protected File resolveFunctionDataRootFolder() {
        return plugin.resolveDataFile(functionName);
    }
    
    protected boolean checkCosPermission(NudgeEvent event) {
        if (event.getSubject() instanceof Group) {
            return checkCosPermission(event.getBot(), (Group)event.getSubject());
        }
        return false;
    }
    
    protected boolean checkCosPermission(Bot bot, Group group) {
        Permission targetPermission = baseBotLogic.getCharacterCosPermission();
        if (targetPermission == null) {
            // may register fail
            return false;
        }
        ExactMember exactGroup = new ExactMember(group.getId(), bot.getId());
        return PermissionService.testPermission(targetPermission, exactGroup);
    }
    
    protected boolean checkCosPermission(GroupEvent event) {
        return checkCosPermission(event.getBot(), event.getGroup());
    }
    
    protected boolean checkCosPermission(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else if (sender instanceof MemberCommandSender) {
            return checkCosPermission(sender.getBot(), ((MemberCommandSender) sender).getGroup());
        }
        return false;
    }

}
