package hundun.miraifleet.framework.starter.botlogic.function.drive;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.config.HourlyChatConfig;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderList;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * 驾驶机器人
 * @author hundun
 * Created on 2022/01/23
 */
@AsCommand
public class DriveFunction extends BaseFunction<Void>{

    public DriveFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "DriveFunction",
            null
            );
        
    }
    
    long currentGroupId;
    long currentBotId;
    
    @SubCommand("设置驾驶目标")
    public void setTargetGroup(CommandSender sender, Long botId, Long groupId) {
        if (!checkCosPermission(sender)) {
            return;
        }
        currentGroupId = groupId;
        currentBotId = botId;
    }
    
    @SubCommand("驾驶")
    public void chat(CommandSender sender, String messageCode) {
        if (!checkCosPermission(sender)) {
            return;
        }
        Bot.findInstance(currentBotId).getGroupOrFail(currentGroupId).sendMessage(MiraiCode.deserializeMiraiCode(messageCode));
    }

}