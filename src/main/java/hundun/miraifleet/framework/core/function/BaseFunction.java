package hundun.miraifleet.framework.core.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.ListenerHost;

/**
 * @author hundun
 * Created on 2021/04/25
 * @param <T>
 */
public abstract class BaseFunction<T> extends CompositeCommand implements ListenerHost {
    
    Supplier<T> sessionDataSupplier;
    protected JvmPlugin plugin;
    public boolean asCompositeCommand;
    public boolean asListenerHost;
    
    
    public BaseFunction(
            JvmPlugin plugin, 
            String characterName, 
            String functionName,
            boolean asCompositeCommand,
            boolean asListenerHost,
            Supplier<T> sessionDataSupplier
            ) {
        super(plugin, functionName, new String[]{characterName}, "我是" + functionName, plugin.getParentPermission(), CommandArgumentContext.EMPTY);
        this.sessionDataSupplier = sessionDataSupplier;
        this.plugin = plugin;
        this.asCompositeCommand = asCompositeCommand;
        this.asListenerHost = asListenerHost;
    }
    
    protected T getOrCreateSessionData(Group group) {
        String sessionId = String.valueOf(group.getId());
        T sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = sessionDataSupplier.get();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }

    
    protected T getOrCreateSessionData(CommandSender sender) {
        String sessionId = "default";
        if (sender instanceof MemberCommandSender) {
            sessionId = String.valueOf(((MemberCommandSender)sender).getGroup().getId());
        }
        
        T sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = sessionDataSupplier.get();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }

    Map<String, T> sessionDataMap = new HashMap<>();
    
    
}
