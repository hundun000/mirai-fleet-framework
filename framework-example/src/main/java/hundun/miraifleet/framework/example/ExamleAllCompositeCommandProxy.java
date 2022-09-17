package hundun.miraifleet.framework.example;

import hundun.miraifleet.framework.core.function.AbstractAllCompositeCommandProxy;
import hundun.miraifleet.framework.starter.botlogic.function.CharacterHelpFunction;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboFunction;
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

    @SubCommand("help")
    public void help(CommandSender sender) {
        botLogic.getFunction(CharacterHelpFunction.class).getCommandComponent().help(sender);
    }


    @SubCommand("刷新微博订阅")
    public void updateAndGetUserInfoCache(CommandSender sender) {
        botLogic.getFunction(WeiboFunction.class).getCommandComponent().updateAndGetUserInfoCache(sender);
    }

    @SubCommand("查询微博订阅")
    public void listListen(CommandSender sender) {
        botLogic.getFunction(WeiboFunction.class).getCommandComponent().listListen(sender);
    }

}
