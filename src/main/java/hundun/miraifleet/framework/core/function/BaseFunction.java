package hundun.miraifleet.framework.core.function;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.SimpleCommand;
import net.mamoe.mirai.console.command.UserCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactMember;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactUser;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/04/25
 * @param <T>
 */
public abstract class BaseFunction<T> implements ListenerHost {

    public static abstract class AbstractSimpleCommandFunctionComponent extends SimpleCommand {
        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                String characterName,
                String functionName,
                String forceCommandName
                ) {
            super(plugin, forceCommandName, new String[]{}, "我是" + functionName, plugin.getParentPermission(), CommandArgumentContext.EMPTY);
        }

        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                String characterName,
                String functionName
                ) {
            this(plugin, characterName, functionName, toCommandName(characterName, functionName));
        }
    }

    private static String toCommandName(String characterName, String functionName) {
        return characterName + functionName;
    }

    public static abstract class AbstractCompositeCommandFunctionComponent extends CompositeCommand {
        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                String characterName,
                String functionName,
                String forceCommandName
                ) {
            super(plugin, forceCommandName, new String[]{}, "我是" + functionName, plugin.getParentPermission(), CommandArgumentContext.EMPTY);
        }
        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                String characterName,
                String functionName
                ) {
            this(plugin, characterName, functionName, toCommandName(characterName, functionName));
        }
    }

    public static final String NOT_SUPPORT_RESOURCE_PLACEHOLDER = "[该终端不支持图片或音频]";
    /**
     * 设计上该Function只使用唯一SESSIONID时，即使用该值。
     */
    public static final String SINGLETON_SESSIONID = "SINGLETON";
    /**
     * 调用者为console时对应的SESSIONID
     */
    public static final String CONSOLE_SESSIONID = "CONSOLE";

    private Supplier<T> sessionDataSupplier;
    protected final JvmPlugin plugin;
    protected final MiraiLogger log;
    protected final BaseBotLogic baseBotLogic;
    protected final String functionName;
    protected final String characterName;
    @Getter
    private final boolean skipRegisterCommand;
    
    Map<String, T> sessionDataMap = new ConcurrentHashMap<>();

    public BaseFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            String functionName,
            boolean skipRegisterCommand,
            Supplier<T> sessionDataSupplier
            ) {
        this.sessionDataSupplier = sessionDataSupplier;
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.baseBotLogic = baseBotLogic;
        this.functionName = functionName;
        this.characterName = characterName;
        this.skipRegisterCommand = skipRegisterCommand;
    }

    public abstract AbstractCommand provideCommand();

    protected String getSessionId(CommandSender sender) {
        String sessionId;
        if (sender instanceof MemberCommandSender) {
            sessionId = String.valueOf(((MemberCommandSender)sender).getGroup().getId());
        } if (sender instanceof ConsoleCommandSender) {
            sessionId = CONSOLE_SESSIONID;
        } else {
            sessionId = String.valueOf(sender.getUser().getId());
        }
        return sessionId;
    }

    protected T getOrCreateSessionData() {
        String sessionId = SINGLETON_SESSIONID;
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
            log.warning("checkCosPermission false because Permission is null, maybe Permission register failed");
            return false;
        }
        ExactMember exactGroup = new ExactMember(group.getId(), bot.getId());
        return PermissionService.testPermission(targetPermission, exactGroup);
    }

    protected boolean checkCosPermission(Bot bot, User user) {
        Permission targetPermission = baseBotLogic.getCharacterCosPermission();
        if (targetPermission == null) {
            log.warning("checkCosPermission false because Permission is null, maybe Permission register failed");
            return false;
        }
        ExactUser exact = new ExactUser(user.getId());
        return PermissionService.testPermission(targetPermission, exact);
    }

    protected boolean checkCosPermission(GroupEvent event) {
        return checkCosPermission(event.getBot(), event.getGroup());
    }

    protected boolean checkCosPermission(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else if (sender instanceof MemberCommandSender) {
            return checkCosPermission(sender.getBot(), ((MemberCommandSender) sender).getGroup());
        } else if (sender instanceof UserCommandSender) {
            return checkCosPermission(sender.getBot(), ((UserCommandSender) sender).getUser());
        }
        return false;
    }

}
