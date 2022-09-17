package hundun.miraifleet.framework.core.function;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.command.UserCommandSender;
import net.mamoe.mirai.contact.AudioSupported;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * 找不到一个同时代表CommandSender和Contact的类，姑且先用本类（装饰器）来实现。作为Function的操作对象（Reply对象），提供uploadImage、uploadVoice、sendMessage等Reply方式。
 */
public class FunctionReplyReceiver {
    private static final String CONSOLE_SENDER_DESCRIPTION = "CONSOLE_SENDER";
    private static final String UNHANDLED_SENDER_DESCRIPTION = "UNHANDLED_SENDER";
    public static final String NOT_SUPPORT_RESOURCE_PLACEHOLDER = "[该接收者暂不支持图片或音频]";
    public final static int CONSOLE_FAKE_CONTACT_ID = 0;
    public final static int CONSOLE_FAKE_BOT_ID = 0;

    private final MiraiLogger miraiLogger;
    private final CommandSender commandSender;
    private final Contact contact;

    @Getter
    private final String senderDescription;

    public FunctionReplyReceiver(CommandSender commandSender, MiraiLogger miraiLogger) {
        this.commandSender = commandSender;
        this.contact = null;
        this.miraiLogger = miraiLogger;
        this.senderDescription = initSenderDescription();
    }

    public FunctionReplyReceiver(Contact contact, MiraiLogger miraiLogger) {
        this.commandSender = null;
        this.contact = contact;
        this.miraiLogger = miraiLogger;
        this.senderDescription = initSenderDescription();
    }

    private String initSenderDescription() {
        String description;
        if (commandSender instanceof MemberCommandSender) {
            description = ((MemberCommandSender)commandSender).getGroup().getId() + "." + ((MemberCommandSender)commandSender).getUser().getId();
        } else if (commandSender instanceof ConsoleCommandSender) {
            description = CONSOLE_SENDER_DESCRIPTION;
        } else if (commandSender instanceof UserCommandSender) {
            description = "u" + ((UserCommandSender)commandSender).getUser().getId();
        } else {
            description = UNHANDLED_SENDER_DESCRIPTION;
        }
        return description;
    }

    /**
     * do nothing if not supported
     */
    public void sendMessage(Message message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
    }

    /**
     * do nothing if not supported
     */
    public void sendMessage(String message) {
        if (commandSender != null) {
            commandSender.sendMessage(message);
        } else if (contact != null) {
            contact.sendMessage(message);
        }
    }

    /**
     * @Deprecated enhanced and renamed
     */
    @Deprecated
    public Image uploadImage(ExternalResource externalResource) {
        return uploadImageAndClose(externalResource);
    }
    
    /**
     * return null if not supported; externalResource will must be closed.
     */
    @Nullable
    public Image uploadImageAndClose(ExternalResource externalResource) {
        Image result = null;
        try {
            if (commandSender != null) {
                if (commandSender.getSubject() != null) {
                    result = commandSender.getSubject().uploadImage(externalResource);
                }
            } else if (contact != null) {
                result = contact.uploadImage(externalResource);
            }
        } catch (Exception e) {
            miraiLogger.error(String.format("uploadImage fail, FormatName = %s", externalResource.getFormatName()), e);
        } finally {
            try {
                externalResource.close();
            } catch (IOException e) {
                miraiLogger.error("externalResource.close fail:", e);
            }
        }
        return result;
    }

    /**
     * @Deprecated enhanced and renamed
     */
    @Deprecated
    @NotNull
    public Message uploadImageOrNotSupportPlaceholder(ExternalResource externalResource) {
        return uploadImageAndCloseOrNotSupportPlaceholder(externalResource);
    }
    
    @NotNull
    public Message uploadImageAndCloseOrNotSupportPlaceholder(ExternalResource externalResource) {
        Image image = uploadImageAndClose(externalResource);
        return image != null ? image : new PlainText(NOT_SUPPORT_RESOURCE_PLACEHOLDER);
    }

    /**
     * @return null if not supported
     */
    @Nullable
    public OfflineAudio uploadVoiceAndClose(ExternalResource externalResource) {
        OfflineAudio result = null;
        try {
            if (commandSender != null) {
                if (commandSender.getSubject() != null 
                        && commandSender.getSubject() instanceof AudioSupported) {
                    result = ((AudioSupported)commandSender.getSubject()).uploadAudio(externalResource);
                }
            } else if (contact != null) {
                if (contact instanceof AudioSupported) {
                    result = ((AudioSupported)contact).uploadAudio(externalResource);
                }
            }
        } catch (Exception e) {
            miraiLogger.error(String.format("uploadAudio fail, FormatName = %s", externalResource.getFormatName()), e);
        } finally {
            try {
                externalResource.close();
            } catch (Exception e) {
                miraiLogger.error("externalResource.close fail:", e);
            }
        }
        return result;
    }

    @NotNull
    public Message uploadVoiceAndCloseOrNotSupportPlaceholder(ExternalResource externalResource) {
        OfflineAudio audio = uploadVoiceAndClose(externalResource);
        return audio != null ? audio : new PlainText(NOT_SUPPORT_RESOURCE_PLACEHOLDER);
    }

    /**
     * return from commandSender, or from contact, or fail.
     */
    public long getUserContactId() {
        if (commandSender != null) {
            return getUserContactId(commandSender);
        } else if (contact != null) {
            return contact.getId();
        }
        throw new RuntimeException("bad FunctionReplyReceiver instance getUserContactId");
    }

    /**
     * return from commandSender, or from contact, or fail.
     */
    public long getBotIdOrConsole() {
        if (commandSender != null) {
            return getBotIdOrConsole(commandSender);
        } else if (contact != null) {
            return contact.getBot().getId();
        }
        throw new RuntimeException("bad FunctionReplyReceiver instance getBotIdOrConsole");
    }




    /**
     * from commandSender.getUser().getId() or CONSOLE_FAKE_ID;
     */
    public static long getUserContactId(CommandSender commandSender) {
        if (commandSender.getUser() != null) {
            return commandSender.getUser().getId();
        } else {
            return CONSOLE_FAKE_CONTACT_ID;
        }
    }

    /**
     * from commandSender.getBot().getId() or CONSOLE_FAKE_ID;
     */
    public static long getBotIdOrConsole(CommandSender sender) {
        return sender.getBot() != null ? sender.getBot().getId() : CONSOLE_FAKE_BOT_ID;
    }


}
