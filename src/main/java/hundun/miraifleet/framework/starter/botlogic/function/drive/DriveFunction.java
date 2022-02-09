package hundun.miraifleet.framework.starter.botlogic.function.drive;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * 驾驶机器人
 * @author hundun
 * Created on 2022/01/23
 */
public class DriveFunction extends BaseFunction<Void>{

    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    public DriveFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            boolean skipRegisterCommand
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "DriveFunction",
            skipRegisterCommand,
            null
            );
        this.commandComponent = new CompositeCommandFunctionComponent(plugin, characterName, functionName);
    }

    long currentGroupId;
    long currentBotId;

    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent(JvmPlugin plugin, String characterName, String functionName) {
            super(plugin, characterName, functionName);
        }

        @SubCommand("设置驾驶状态")
        public void setTargetGroup(CommandSender sender, Long botId, Long groupId) {
            if (!checkCosPermission(sender)) {
                return;
            }
            currentGroupId = groupId;
            currentBotId = botId;
            sender.sendMessage("OK");
        }

        @SubCommand("查看驾驶状态")
        public void listTargetGroup(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            sender.sendMessage("当前驾驶状态 BotId = " + currentBotId + ", GroupId = " + currentGroupId);
        }

        @SubCommand("驾驶")
        public void chat(CommandSender sender, String messageCode) {
            if (!checkCosPermission(sender)) {
                return;
            }
            if (sender.getBot().getId() == currentBotId) {
                sender.getBot().getGroupOrFail(currentGroupId).sendMessage(MiraiCode.deserializeMiraiCode(messageCode));
            }
        }

    }



}
