package hundun.miraifleet.framework.starter.botlogic.function;

import org.jetbrains.annotations.NotNull;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * @author hundun
 * Created on 2021/04/21
 */
@AsListenerHost
public class RepeatFunction extends BaseFunction<RepeatFunction.SessionData> {

    public static class SessionData {
        public String messageMiraiCode = "";
        public int count = 0;
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
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception {
        if (!checkCosPermission(event)) {
            return; 
        }
        
        SessionData sessionData = getOrCreateSessionData(event.getGroup());
        String newMessageMiraiCode = event.getMessage().serializeToMiraiCode();
        
        if (sessionData.messageMiraiCode.equals(newMessageMiraiCode)) {
            sessionData.count++;
        } else {
            sessionData.count = 1;
            sessionData.messageMiraiCode = newMessageMiraiCode;
        }

        
        if (sessionData.count == 3) {
            event.getGroup().sendMessage(MiraiCode.deserializeMiraiCode(sessionData.messageMiraiCode));
        }
    }

}
