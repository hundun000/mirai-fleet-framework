package hundun.miraifleet.framework.starter.botlogic.function;


import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactMember;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.AnyMember;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;

/**
 * @author hundun
 *
 */
public class CharacterAdminHelperFunction extends BaseFunction<Void> {
    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    public CharacterAdminHelperFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "CharacterAdminHelperFunction",
            null
            );
        this.setSkipRegisterCommand(false);
        this.commandComponent = new CompositeCommandFunctionComponent();
    }
    
    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent() {
            super(plugin, 
                    botLogic.getAdminCommandRootPermission(), 
                    characterName, 
                    functionName, 
                    characterName + "权限助手"
                    );
        }
        
        @Description("变更群开关")
        @SubCommand("群开关Console")
        public void groupEnableSetterConsole(ConsoleCommandSender sender, boolean setToEnable, long botId, long groupId) {
            generalGroupEnableSetter(sender, setToEnable, groupId, botId);
        }
        
        @Description("变更群开关")
        @SubCommand("群开关")
        public void groupEnableSetter(CommandSender sender, boolean setToEnable, Group targetGroup) {
            generalGroupEnableSetter(sender, setToEnable, targetGroup.getId(), targetGroup.getId());
        }

        private void generalGroupEnableSetter(CommandSender sender, boolean setToEnable, long groupId, long botId) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }

            try {
                if (setToEnable) {
                    PermissionService.permit(new AnyMember(groupId), 
                            botLogic.getUserCommandRootPermission().getId());
                    PermissionService.permit(new ExactMember(groupId, botId), 
                            botLogic.getCharacterCosPermission().getId());
                } else {
                    PermissionService.cancel(new AnyMember(groupId), 
                            botLogic.getUserCommandRootPermission().getId(),
                            false);
                    PermissionService.cancel(new ExactMember(groupId, botId), 
                            botLogic.getCharacterCosPermission().getId(),
                            false);
                }
                
                sender.sendMessage("成功");
            } catch (Exception e) {
                log.error(e);
                sender.sendMessage("操作期间发生异常，失败");
            }

        }
    }
}
