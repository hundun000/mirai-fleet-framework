package hundun.miraifleet.frame.starter.botlogic.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hundun.miraifleet.frame.core.data.EventInfo;
import hundun.miraifleet.frame.core.data.SessionId;
import hundun.miraifleet.frame.core.function.BaseFunction;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * @author hundun
 * Created on 2021/05/19
 */
public class MiraiCodeFunction extends BaseFunction<MiraiCodeFunction.SessionData> {
        
    public MiraiCodeFunction(
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            plugin,
            characterName,
            "MiraiCodeFunction",
            true,
            true,
            (() -> new MiraiCodeFunction.SessionData())
            );
    }

    
    
    public static class SessionData {
        public String messageMiraiCode = "";
    }
    
    @SubCommand("解码")
    public boolean decode(CommandSender sender, String miraiCode) {
        plugin.getLogger().info("build MessageChain by miraiCode = " + miraiCode);
        MessageChain chain = MiraiCode.deserializeMiraiCode(miraiCode);
        sender.sendMessage(chain);
        return true;
    }
    
    @SubCommand("编码")
    public boolean encode(CommandSender sender) {
        SessionData sessionData = getOrCreateSessionData(sender);
        String miraiCode = sessionData.messageMiraiCode;
        sender.sendMessage(miraiCode);
        return true;
    }
    
    @EventHandler
    public void onMessage(@NotNull GroupMessageEvent event) throws Exception { 
        SessionData sessionData = getOrCreateSessionData(event.getGroup());
        sessionData.messageMiraiCode = event.getMessage().serializeToMiraiCode();
    }


}
