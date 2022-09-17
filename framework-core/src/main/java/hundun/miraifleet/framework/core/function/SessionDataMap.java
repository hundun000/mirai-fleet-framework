package hundun.miraifleet.framework.core.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.MessagePostSendEvent;

/**
 * @author hundun
 * Created on 2022/09/19
 * @param <T>
 */
public class SessionDataMap<T> {
    /**
     * 设计上该Function只使用唯一SESSIONID时，即使用该值。
     */
    public static final String SINGLETON_SESSIONID = "SINGLETON";
    /**
     * 调用者为console时对应的SESSIONID
     */
    public static final String CONSOLE_SESSIONID = "CONSOLE";
    
    Map<String, T> sessionDataMap = new ConcurrentHashMap<>();
    
    private final Supplier<T> sessionDataSupplier;
    private final GroupMessageToSessionIdType type;
    
    public SessionDataMap(
            GroupMessageToSessionIdType type,
            Supplier<T> sessionDataSupplier
            ) {
        this.type = type;
        this.sessionDataSupplier = sessionDataSupplier;
    }
    
    private String memberToSessionId(long groupId, long memberId) {
        if (type == GroupMessageToSessionIdType.USE_GROUP_ID) {
            return String.valueOf(groupId);
        } else {
            return String.valueOf(groupId)
                    + "." + String.valueOf(memberId);
        }
    }
    
    public String calculateSessionId(MessageEvent event) {
        String sessionId;
        if (event instanceof GroupMessageEvent) {
            GroupMessageEvent groupMessageEvent = (GroupMessageEvent)event;
            sessionId = memberToSessionId(groupMessageEvent.getGroup().getId(), groupMessageEvent.getSender().getId());
        } else {
            sessionId = String.valueOf(event.getSender().getId());
        }
        return sessionId;
    }
    
    public String getSessionId(CommandSender sender) {
        String sessionId;
        if (sender instanceof MemberCommandSender) {
            MemberCommandSender memberCommandSender = (MemberCommandSender)sender;
            sessionId = memberToSessionId(memberCommandSender.getGroup().getId(), memberCommandSender.getUser().getId());
        } if (sender instanceof ConsoleCommandSender) {
            sessionId = CONSOLE_SESSIONID;
        } else {
            sessionId = String.valueOf(sender.getUser().getId());
        }
        return sessionId;
    }
    
    public String calculateSessionId(MessagePostSendEvent<? extends Contact> event) {
        String sessionId;
        if (event instanceof GroupMessagePostSendEvent) {
            GroupMessagePostSendEvent groupMessageEvent = (GroupMessagePostSendEvent)event;
            sessionId = memberToSessionId(groupMessageEvent.getTarget().getId(), groupMessageEvent.getBot().getId());
        } else {
            sessionId = String.valueOf(event.getBot().getId());
        }
        return sessionId;
    }
    
    public T getOrCreateSessionData() {
        String sessionId = SINGLETON_SESSIONID;
        return getOrCreateSessionData(sessionId);
    }

    public T getOrCreateSessionData(MessageEvent event) {
        String sessionId = calculateSessionId(event);
        return getOrCreateSessionData(sessionId);
    }
    
    public T getOrCreateSessionData(MessagePostSendEvent<? extends Contact> event) {
        String sessionId = calculateSessionId(event);
        return getOrCreateSessionData(sessionId);
    }

    public T getOrCreateSessionData(CommandSender sender) {
        String sessionId = getSessionId(sender);
        return getOrCreateSessionData(sessionId);
    }

    public T getOrCreateSessionData(String sessionId) {
        T sessionData = sessionDataMap.get(sessionId);
        if (sessionData == null) {
            sessionData = sessionDataSupplier.get();
            sessionDataMap.put(sessionId, sessionData);
        }
        return sessionData;
    }

    protected T removeSessionData(String sessionId) {
        T sessionData = sessionDataMap.remove(sessionId);
        return sessionData;
    }
    
    public static enum GroupMessageToSessionIdType {
        USE_GROUP_ID,
        USE_GROUP_AND_MEMBER_ID
    }
}
