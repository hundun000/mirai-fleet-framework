package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.function.AbstractAllCompositeCommandProxy;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * 目前只能用唯一的CompositeCommand注册所有SubCommand，未来改为分别注册。
 * https://github.com/mamoe/mirai-console/issues/397
 * @author hundun
 * Created on 2021/08/11
 */
public class ExamleAllCompositeCommandProxy extends AbstractAllCompositeCommandProxy<ExampleBotLogic> {


    public ExamleAllCompositeCommandProxy(
            ExampleBotLogic botLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(botLogic, plugin, characterName);
    }

    @SubCommand("查询报时")
    public void listHourlyChatConfig(CommandSender sender) {
        botLogic.reminderFunction.getCommandComponent().listHourlyChatConfig(sender);
    }


    @SubCommand("微博订阅")
    public void listListen(CommandSender sender) {
        botLogic.weiboFunction.getCommandComponent().listListen(sender);
    }


}
