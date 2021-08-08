package hundun.miraifleet.framework.starter.botlogic.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import hundun.miraifleet.framework.core.data.EventInfo;
import hundun.miraifleet.framework.core.data.SessionId;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.starter.botlogic.function.MiraiCodeFunction.SessionData;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * @author hundun
 * Created on 2021/04/21
 */
public class RepeatFunction extends BaseFunction<RepeatFunction.SessionData> {
        

    public static class SessionData {
        public String messageMiraiCode = "";
        public int count = 0;
    }
    
    public RepeatFunction(
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            plugin, 
            characterName,
            "RepeatConsumer",
            false,
            true,
            (() -> new RepeatFunction.SessionData())
            );
    }
    
    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception { 
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
