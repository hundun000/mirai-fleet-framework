package hundun.miraifleet.framework.starter.botlogic.function;

import org.jetbrains.annotations.NotNull;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * @author hundun
 * Created on 2021/04/21
 */
@AsListenerHost
public class RepeatFunction extends BaseFunction<RepeatFunction.SessionData> {

    final static int COUNT_LIMIT = 3;

    public static class SessionData {
        public RepeatState state = RepeatState.CURRENT_MESSAGE_HANDLED;
        public String messageMiraiCode = "";
        public int count = -1;
    }

    public static enum RepeatState {
        CURRENT_MESSAGE_HANDLED,
        CURRENT_MESSAGE_COUNTING,
        ;
    }

    public RepeatFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "RepeatFunction",
            (() -> new RepeatFunction.SessionData())
            );
    }

    @EventHandler
    public void onMessage(@NotNull GroupMessagePostSendEvent event) throws Exception {
        Group group = event.getTarget();
        if (!checkCosPermission(event.getBot(), group)) {
            return;
        }

        SessionData sessionData = getOrCreateSessionData(event);


        if (sessionData.state == RepeatState.CURRENT_MESSAGE_COUNTING) {
            sessionData.state = RepeatState.CURRENT_MESSAGE_HANDLED;
            sessionData.count = -1;
            // keep same sessionData.messageMiraiCode
        }
    }


    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception {
        if (!checkCosPermission(event)) {
            return;
        }

        SessionData sessionData = getOrCreateSessionData(event);
        String newMessageMiraiCode = event.getMessage().serializeToMiraiCode();

        switch (sessionData.state) {
            case CURRENT_MESSAGE_HANDLED:
                if (!sessionData.messageMiraiCode.equals(newMessageMiraiCode)) {
                    sessionData.state = RepeatState.CURRENT_MESSAGE_COUNTING;
                    sessionData.messageMiraiCode = newMessageMiraiCode;
                    sessionData.count = 1;
                }
                break;
            case CURRENT_MESSAGE_COUNTING:
                if (sessionData.messageMiraiCode.equals(newMessageMiraiCode)) {
                    sessionData.count++;
                    if (sessionData.count >= COUNT_LIMIT) {
                        event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(sessionData.messageMiraiCode));
                        sessionData.state = RepeatState.CURRENT_MESSAGE_HANDLED;
                        sessionData.count = -1;
                    }
                } else {
                    sessionData.messageMiraiCode = newMessageMiraiCode;
                    sessionData.count = 1;
                }
                break;
            default:
                break;
        }




    }

    @Override
    public AbstractCommand provideCommand() {
        return null;
    }

}
