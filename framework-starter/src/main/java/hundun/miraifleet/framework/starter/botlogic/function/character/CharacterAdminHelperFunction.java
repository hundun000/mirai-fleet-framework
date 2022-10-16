package hundun.miraifleet.framework.starter.botlogic.function.character;


import java.util.HashSet;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.helper.repository.SingletonDocumentRepository;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactMember;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.AnyMember;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.BotJoinGroupEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;

/**
 * @author hundun
 *
 */
@AsListenerHost
public class CharacterAdminHelperFunction extends BaseFunction {
    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    private final SingletonDocumentRepository<CharacterAdminHelperFunctionConfig> functionConfigRepository;
    public CharacterAdminHelperFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "CharacterAdminHelperFunction"
            );
        this.setSkipRegisterCommand(false);
        this.commandComponent = new CompositeCommandFunctionComponent();
        this.functionConfigRepository = new SingletonDocumentRepository<>(
                plugin, 
                resolveConfigRepositoryFile("FunctionConfig.json"), 
                CharacterAdminHelperFunctionConfig.class, 
                () -> CharacterAdminHelperFunctionConfig.builder().permittedGroupInviters(new HashSet<>()).build()
                );
    }
    
    @EventHandler
    public void onBotInvitedJoinGroupRequest(@NotNull BotInvitedJoinGroupRequestEvent event) throws Exception {
        var config = functionConfigRepository.findSingleton();
        if (config.getPermittedGroupInviters().contains(event.getInvitorId())) {
            log.info("InvitedJoinGroupRequest, 即将自动同意");
            event.accept();
        } else {
            log.info("InvitedJoinGroupRequest, 不会自动同意");
        }
    }
    
    @EventHandler
    public void onBotJoinGroupInvite(@NotNull BotJoinGroupEvent.Invite event) throws Exception {
        var config = functionConfigRepository.findSingleton();
        if (config.getPermittedGroupInviters().contains(event.getInvitor().getId())) {
            try {
                commandComponent.generalGroupEnableSetter(true, event.getGroupId(), event.getBot().getId());
                log.info("BotJoinGroupEvent.Invite, 自动开启群开关成功");
            } catch (Exception e) {
                log.error("BotJoinGroupEvent.Invite, 自动开启群开关期间发生异常，失败", e);
            }
        } else {
            log.info("BotJoinGroupEvent.Invite, 不会自动同意");
        }
    }
    
    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent() {
            super(plugin, botLogic, new AdminLevelFunctionComponentConstructPack(
                            characterName, 
                            functionName, 
                            "权限助手"
                            )
                    );
        }
        
        @Description("变更群开关")
        @SubCommand("群开关Console")
        public void groupEnableSetterConsole(ConsoleCommandSender sender, 
                @Name("setToEnable") boolean setToEnable, 
                @Name("botId") long botId, 
                @Name("groupId") long groupId) {
            generalGroupEnableSetterFromSender(sender, setToEnable, groupId, botId);
        }
        
        @Description("变更群开关")
        @SubCommand("群开关")
        public void groupEnableSetter(CommandSender sender, 
                @Name("setToEnable") boolean setToEnable, 
                @Name("Group智能参数解析") Group targetGroup) {
            generalGroupEnableSetterFromSender(sender, setToEnable, targetGroup.getId(), targetGroup.getBot().getId());
        }

        @SubCommand("添加邀请者")
        public void addInviter(ConsoleCommandSender sender, 
                @Name("inviterId") long inviterId) {
            var config = functionConfigRepository.findSingleton();
            config.getPermittedGroupInviters().add(inviterId);
            functionConfigRepository.saveSingleton(config);
            
            sender.sendMessage(String.format(
                    "PermittedGroupInviters: %s",
                    config.getPermittedGroupInviters()
                    ));
        }
        
        @SubCommand("移除邀请者")
        public void removeInviter(ConsoleCommandSender sender, 
                @Name("inviterId") long inviterId) {
            var config = functionConfigRepository.findSingleton();
            config.getPermittedGroupInviters().remove(inviterId);
            functionConfigRepository.saveSingleton(config);
            
            sender.sendMessage(String.format(
                    "PermittedGroupInviters: %s",
                    config.getPermittedGroupInviters()
                    ));
        }
        
        @Description("展示关键权限节点id")
        @SubCommand("help")
        public void help(CommandSender sender) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }
            sender.sendMessage(String.format(
                    "CharacterCos: %s\n"
                    + "UserCommandRoot: %s\n"
                    + "AdminCommandRoot: %s\n", 
                    botLogic.getCharacterCosPermission().getId().toString(),
                    botLogic.getUserCommandRootPermission().getId().toString(),
                    botLogic.getAdminCommandRootPermission().getId().toString()
                    ));
        }
        
        private void generalGroupEnableSetterFromSender(CommandSender sender, boolean setToEnable, long groupId, long botId) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }

            try {
                generalGroupEnableSetter(setToEnable, groupId, botId);

                sender.sendMessage("成功");
            } catch (Exception e) {
                log.error(e);
                sender.sendMessage("操作期间发生异常，失败");
            }
        }
        
        private void generalGroupEnableSetter(boolean setToEnable, long groupId, long botId) {

            if (setToEnable) {
                PermissionService.permit(new AnyMember(groupId), 
                        botLogic.getUserCommandRootPermission().getId());
                PermissionService.permit(new ExactMember(groupId, botId), 
                        botLogic.getCharacterCosPermission().getId());
            } else {
                try {
                    PermissionService.cancel(new AnyMember(groupId), 
                            botLogic.getUserCommandRootPermission().getId(),
                            true);
                } catch (NoSuchElementException | UnsupportedOperationException e) {
                    log.info("skip cancel UserCommandRootPermission， 原因: " + e.getMessage());
                }
                try {
                    PermissionService.cancel(new ExactMember(groupId, botId), 
                            botLogic.getCharacterCosPermission().getId(),
                            true);
                } catch (NoSuchElementException | UnsupportedOperationException e) {
                    log.info("skip cancel CharacterCosPermission， 原因: " + e.getMessage());
                }
            }

        }
    }
}
