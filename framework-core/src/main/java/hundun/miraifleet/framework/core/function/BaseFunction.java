package hundun.miraifleet.framework.core.function;

import java.io.File;
import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/04/25
 * @param <T>
 */
public abstract class BaseFunction implements ListenerHost {
    
    @Getter
    @AllArgsConstructor
    public static class UserLevelFunctionComponentConstructPack {
        private final String characterName;
        private final String functionName;
        public String toCommandName() {
            return characterName + functionName;
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class DebugLevelFunctionComponentConstructPack {
        private final String characterName;
        private final String functionName;
        public String toCommandName() {
            return characterName + functionName + "Debug";
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class AdminLevelFunctionComponentConstructPack {
        private final String characterName;
        private final String functionName;
        private final String customFunctionName;
        public String toCommandName() {
            return characterName + customFunctionName;
        }
    }
    
    public static abstract class AbstractSimpleCommandFunctionComponent extends SimpleCommand {
        private AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                Permission parentPermission,
                String characterName,
                String functionName,
                String forceCommandName
                ) {
            super(plugin, 
                    forceCommandName, 
                    new String[]{}, 
                    "略", 
                    parentPermission,
                    CommandArgumentContext.EMPTY);
        }

        /**
         * @Deprecated use UserLevelFunctionComponentConstructPack
         */
        @Deprecated
        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                String characterName,
                String functionName
                ) {
            this(plugin, 
                    baseBotLogic,
                    new UserLevelFunctionComponentConstructPack(characterName, functionName)
                    );
        }
        
        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                UserLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getUserCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
        
        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                DebugLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getAdminCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
        
        public AbstractSimpleCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                AdminLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getAdminCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
    }

    public static abstract class AbstractCompositeCommandFunctionComponent extends CompositeCommand {
        private AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                Permission parentPermission,
                String characterName,
                String functionName,
                String forceCommandName
                ) {
            super(plugin, 
                    forceCommandName, 
                    new String[]{}, 
                    "略", 
                    parentPermission, 
                    CommandArgumentContext.EMPTY
                    );
        }

        /**
         * @Deprecated use UserLevelFunctionComponentConstructPack
         */
        @Deprecated
        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                String characterName,
                String functionName
                ) {
            this(plugin, 
                    baseBotLogic,
                    new UserLevelFunctionComponentConstructPack(characterName, functionName)
                    );
        }

        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                UserLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getUserCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
        
        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                DebugLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getAdminCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
        
        public AbstractCompositeCommandFunctionComponent(
                JvmPlugin plugin,
                BaseBotLogic baseBotLogic,
                AdminLevelFunctionComponentConstructPack constructPack
                ) {
            this(plugin, 
                    baseBotLogic.getAdminCommandRootPermission(),
                    constructPack.getCharacterName(), 
                    constructPack.getFunctionName(), 
                    constructPack.toCommandName()
                    );
        }
    }

    public static final String NOT_SUPPORT_RESOURCE_PLACEHOLDER = "[该终端不支持图片或音频]";
    

    
    protected final JvmPlugin plugin;
    protected final MiraiLogger log;
    protected final BaseBotLogic botLogic;
    protected final String functionName;
    protected final String characterName;
    @Getter
    @Setter
    private boolean skipRegisterCommand;
//    @Getter
//    @Setter
//    private boolean skipRegisterDebugCommand;
    @Getter
    @Setter
    private boolean forcePassCosCheck;

    public BaseFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            String functionName
            ) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.botLogic = baseBotLogic;
        this.functionName = functionName;
        this.characterName = characterName;
        // default values
        this.skipRegisterCommand = true;
        this.forcePassCosCheck = false;
    }

    public abstract AbstractCommand provideCommand();
    public AbstractCommand provideDebugCommand() {
        return null;
    };
    
    protected File resolveFunctionConfigRootFolder() {
        return plugin.resolveConfigFile(functionName);
    }

    protected File resolveFunctionConfigFile(String jsonFileName) {
        return plugin.resolveConfigFile(functionName + File.separator + jsonFileName);
    }

    protected File resolveFunctionCacheFileFolder() {
        return plugin.resolveDataFile(functionName + File.separator + "caches");
    }
    
    protected File resolveDataRepositoryFile(String jsonFileName) {
        return plugin.resolveDataFile(functionName + File.separator + "repositories" + File.separator + jsonFileName);
    }
    
    protected File resolveConfigRepositoryFile(String jsonFileName) {
        return plugin.resolveConfigFile(functionName + File.separator + "repositories" + File.separator + jsonFileName);
    }

    protected File resolveFunctionDataFile(String fileName) {
        return plugin.resolveDataFile(functionName + File.separator + fileName);
    }

    protected File resolveFunctionDataRootFolder() {
        return plugin.resolveDataFile(functionName);
    }

    protected boolean checkCosPermission(NudgeEvent event) {
        if (forcePassCosCheck) {
            return true;
        }
        if (event.getSubject() instanceof Group) {
            return checkCosPermission(event.getBot(), (Group)event.getSubject());
        } else if (event.getSubject() instanceof User) {
            return checkCosPermission(event.getBot(), (User)event.getSubject());
        }
        return false;
    }

    protected boolean checkCosPermission(Bot bot, Group group) {
        if (forcePassCosCheck) {
            return true;
        }
        Permission targetPermission = botLogic.getCharacterCosPermission();
        ExactMember exactGroup = new ExactMember(group.getId(), bot.getId());
        return PermissionService.testPermission(targetPermission, exactGroup);
    }

    protected boolean checkCosPermission(Bot bot, User user) {
        if (forcePassCosCheck) {
            return true;
        }
        Permission targetPermission = botLogic.getCharacterCosPermission();
        ExactUser exact = new ExactUser(user.getId());
        return PermissionService.testPermission(targetPermission, exact);
    }
    
    protected boolean checkAdminCommandPermission(CommandSender sender) {
        if (forcePassCosCheck) {
            return true;
        }
        return PermissionService.testPermission(
                botLogic.getAdminCommandRootPermission(), 
                sender.getPermitteeId());
    }
  
    protected boolean checkCosPermission(MessageEvent event) {
        if (forcePassCosCheck) {
            return true;
        }
        if (event.getSubject() instanceof Group) {
            return checkCosPermission(event.getBot(), (Group)event.getSubject());
        } else if (event.getSubject() instanceof User) {
            return checkCosPermission(event.getBot(), (User)event.getSubject());
        }
        return false;
    }

    protected boolean checkCosPermission(CommandSender sender) {
        if (forcePassCosCheck) {
            return true;
        }
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
