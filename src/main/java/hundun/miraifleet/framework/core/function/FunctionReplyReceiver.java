package hundun.miraifleet.framework.core.function;

import java.io.IOException;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.Voice;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * 找不到一个同时代表CommandSender和Contact的类，姑且先用本类来实现
 */
public class FunctionReplyReceiver {
    MiraiLogger miraiLogger;
    CommandSender commandSender;
    Contact contact;
    
    
    public FunctionReplyReceiver(CommandSender commandSender, MiraiLogger miraiLogger) {
        this.commandSender = commandSender;
        this.miraiLogger = miraiLogger;
    }
    
    public FunctionReplyReceiver(Contact contact, MiraiLogger miraiLogger) {
        this.contact = contact;
        this.miraiLogger = miraiLogger;
    }
    
    
    public void sendMessage(Message message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
    }

    public void sendMessage(String message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
    }

    public Image uploadImage(ExternalResource externalResource) {
        if (commandSender != null) {
            if (commandSender instanceof MemberCommandSender) {
                return ((MemberCommandSender)commandSender).getGroup().uploadImage(externalResource);
            }
        } else if (contact != null) {
            return contact.uploadImage(externalResource);
        }
        return null;
    }
    
    public Image uploadImageAndClose(ExternalResource externalResource) {
        Image image = uploadImage(externalResource);
        try {
            externalResource.close();
        } catch (IOException e) {
            miraiLogger.error(e);
        }
        return image;
    }
    
    public Voice uploadVoice(ExternalResource externalResource) {
        if (commandSender != null) {
            if (commandSender instanceof MemberCommandSender) {
                return ((MemberCommandSender)commandSender).getGroup().uploadVoice(externalResource);
            }
        } else if (contact != null) {
            if (contact instanceof Group) {
                return ((Group)contact).uploadVoice(externalResource);
            }
        }
        return null;
    }
    
    public long getContactId() {
        if (commandSender != null) {
            return getContactId(commandSender);
        } else if (contact != null) {
            return contact.getId();
        }
        return -1;
    }
    
    public long getBotId() {
        if (commandSender != null) {
            return getBotId(commandSender);
        } else if (contact != null) {
            return contact.getBot().getId();
        }
        return -1;
    }
    
    public final static int CONSOLE_FAKE_CONTACT_ID = 0;
    public final static int CONSOLE_FAKE_BOT_ID = 0;
    
    public static long getContactId(CommandSender commandSender) {
        if (commandSender instanceof MemberCommandSender) {
            return ((MemberCommandSender)commandSender).getGroup().getId();
        }
        if (commandSender instanceof ConsoleCommandSender) {
            return CONSOLE_FAKE_CONTACT_ID;
        }
        return -1;
    }

    public static long getBotId(CommandSender sender) {
        return sender.getBot() != null ? sender.getBot().getId() : CONSOLE_FAKE_BOT_ID;
    }
    
}
