package hundun.miraifleet.framework.starter.botlogic.function.drive;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * 驾驶机器人
 * @author hundun
 * Created on 2022/01/23
 */
public class DriveFunction extends BaseFunction {

    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    public DriveFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "DriveFunction"
            );
        this.commandComponent = new CompositeCommandFunctionComponent();
    }


    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent() {
            super(plugin, botLogic, characterName, functionName);
        }


        @SubCommand("立刻私聊")
        public void chat(CommandSender sender, User target, String messageCode) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }
            target.sendMessage(MiraiCode.deserializeMiraiCode(messageCode));
        }
        
        @SubCommand("立刻群聊")
        public void chat(CommandSender sender, Group target, String messageCode) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }
            target.sendMessage(MiraiCode.deserializeMiraiCode(messageCode));
        }

    }



}
