package hundun.miraifleet.framework.starter.botlogic.function;

import org.jetbrains.annotations.NotNull;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import lombok.Getter;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * @author hundun
 * Created on 2021/05/19
 */
@AsListenerHost
public class MiraiCodeFunction extends BaseFunction<MiraiCodeFunction.SessionData> {

    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    public MiraiCodeFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "MiraiCodeFunction",
            false,
            (() -> new MiraiCodeFunction.SessionData())
            );
        this.commandComponent = new CompositeCommandFunctionComponent(plugin, characterName, functionName);
    }



    public static class SessionData {
        public String messageMiraiCode = "";
    }

    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent(JvmPlugin plugin, String characterName, String functionName) {
            super(plugin, characterName, functionName);
        }

        @SubCommand("解码")
        public void decode(CommandSender sender, String miraiCode) {
            if (!checkCosPermission(sender)) {
                return;
            }
            plugin.getLogger().info("build MessageChain by miraiCode = " + miraiCode);
            MessageChain chain = MiraiCode.deserializeMiraiCode(miraiCode);
            sender.sendMessage(chain);
            return;
        }



        @SubCommand("编码")
        public void encode(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            SessionData sessionData = getOrCreateSessionData(sender);
            String miraiCode = sessionData.messageMiraiCode;
            sender.sendMessage(miraiCode);
            return;
        }

    }

    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception {
        if (!checkCosPermission(event)) {
            return;
        }
        SessionData sessionData = getOrCreateSessionData(event.getGroup());
        sessionData.messageMiraiCode = event.getMessage().serializeToMiraiCode();
    }


}
