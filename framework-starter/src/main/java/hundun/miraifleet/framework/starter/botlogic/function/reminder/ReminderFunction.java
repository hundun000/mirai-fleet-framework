package hundun.miraifleet.framework.starter.botlogic.function.reminder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.function.FunctionReplyReceiver;
import hundun.miraifleet.framework.helper.repository.SingletonDocumentRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.config.HourlyChatConfig;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.data.HourlyChatConfigV2;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.data.ReminderItem;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.data.ReminderList;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

/**
 * @author hundun
 * Created on 2021/08/13
 */
public class ReminderFunction extends BaseFunction<Void> {

    SingletonDocumentRepository<ReminderList> reminderListRepository;
    private SingletonDocumentRepository<HourlyChatConfigV2> configRepository;
    List<ReminderItem> hourlyChatReminderItems = new ArrayList<>();
    
    private Map<String, CronExpression> cronExpressionCaches = new HashMap<>();
    @Setter
    private boolean logMinuteClockArrival = false;
    @Getter
    private final CompositeCommandFunctionComponent commandComponent;
    
    private final ReminderMessageCodeParser reminderMessageCodeParser;
    
    public ReminderFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            @Nullable Supplier<ReminderList> reminderListDefaultDataSupplier,
            @Nullable Supplier<HourlyChatConfigV2> hourlyChatConfigDefaultDataSupplier
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "ReminderFunction",
            null
            );
        this.reminderListRepository = new SingletonDocumentRepository<>(plugin, 
                resolveDataRepositoryFile("ReminderListRepository.json"), 
                ReminderList.class, 
                reminderListDefaultDataSupplier);
        this.configRepository = new SingletonDocumentRepository<>(plugin, 
                resolveFunctionConfigFile("HourlyChatConfigV2.json"), 
                HourlyChatConfigV2.class, 
                hourlyChatConfigDefaultDataSupplier);
        botLogic.getPluginScheduler().repeating(60 * 1000, new ReminderTimerTask());
        this.commandComponent = new CompositeCommandFunctionComponent();
        this.reminderMessageCodeParser = new ReminderMessageCodeParser();
        initHourlyChatConfigToReminderItems();
    }

    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {
        public CompositeCommandFunctionComponent() {
            super(plugin, botLogic, characterName, functionName);
        }

        @SubCommand("查询报时")
        public void listHourlyChatConfig(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            sender.sendMessage(itemModelsToText(hourlyChatReminderItems));
        }

        @SubCommand("查询提醒")
        public void listReminderListChatConfig(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            ReminderList reminderList  = reminderListRepository.findSingleton();
            sender.sendMessage(itemsToText(reminderList.getItems()));
        }

        @SubCommand("删除提醒")
        public void deleteReminderListChatConfig(CommandSender sender, int id) {
            if (!checkCosPermission(sender)) {
                return;
            }
            ReminderList reminderList  = reminderListRepository.findSingleton();
            reminderList.getItems().remove(id);
            sender.sendMessage("OK");
        }

        @SubCommand("创建提醒")
        public void insertReminderListChatConfig(CommandSender sender,
                String cornRawFomat,
                String countRawFomat,
                String text
                ) {
            if (!checkCosPermission(sender)) {
                return;
            }
            Integer count;
            if (countRawFomat.contains("无限")) {
                count = null;
            } else {
                try {
                    countRawFomat = countRawFomat.replace("次", "");
                    count = Integer.valueOf(countRawFomat);
                } catch (Exception e) {
                    sender.sendMessage("参数格式不正确：" + countRawFomat);
                    return;
                }
            }
            ReminderList reminderList  = reminderListRepository.findSingleton();
            reminderList.getItems().add(ReminderItem.Factory.create(cornRawFomat, text, count));
            reminderListRepository.saveSingleton(reminderList);
            sender.sendMessage("OK");
        }

        @SubCommand("debugTimerCallReminderItem")
        public void debugTimerCallReminderItem(CommandSender sender, String timeString) {
            if (!checkAdminCommandPermission(sender)) {
                return;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日H时m分");
            Date date;
            try {
                date = dateFormat.parse(timeString);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            logHourlyHeatBeat(calendar);
            hourlyChatClockArrive(calendar);
            customRemiderClockArrive(calendar);
        }
    }




    private static String itemsToText(List<ReminderItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("items:\n");
        for (int i = 0; i < items.size(); i++) {
            ReminderItem item = items.get(i);
            builder.append("id:").append(i).append("\t");
            builder.append(item.toString()).append("\n");
        }
        return builder.toString();
    }






    private String itemModelsToText(List<ReminderItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("items:\n");
        for (int i = 0; i < items.size(); i++) {
            ReminderItem item = items.get(i);
            builder.append("id:").append(i).append("\t");
            builder.append(item.toString()).append("\n");
        }
        return builder.toString();
    }

    private CronExpression getCronExpression(String cronText) {
        if (!cronExpressionCaches.containsKey(cronText)) {
            try {
                CronExpression cronExpression = new CronExpression(cronText);
                cronExpressionCaches.put(cronText, cronExpression);
            } catch (ParseException e) {
                log.error(e);
            }

        }
        return cronExpressionCaches.get(cronText);
    }


//    @SuppressWarnings("deprecation")
    private void initHourlyChatConfigToReminderItems() {
        HourlyChatConfigV2 config = configRepository.findSingleton();

        if (config.getItems() != null) {
            config.getItems().forEach(it -> {
                hourlyChatReminderItems.add(it);
            });
        }
    }

    private boolean useReminderList(List<ReminderItem> items, Collection<Bot> bots, Calendar now) {
        Iterator<ReminderItem> iterator = items.iterator();
        boolean modified = false;
        while (iterator.hasNext()) {
            ReminderItem reminderItem = iterator.next();
            if (!checkTimeConditions(reminderItem, now) || (reminderItem.getCount() != null && reminderItem.getCount() == 0)) {
                continue;
            }
            for (Bot bot: bots) {
                useReminderItem(reminderItem, bot, now);
            }
            if (reminderItem.getCount() != null && reminderItem.getCount() > 0) {
                reminderItem.setCount(reminderItem.getCount() - 1);
                log.info("reminderItem(text=" + reminderItem.getText() + ") count cahnge to " + reminderItem.getCount());
                modified = true;
                if (reminderItem.getCount() == 0) {
                    iterator.remove();
                }
            }
        }
        return modified;
    }

    private void useReminderItem(ReminderItem reminderItem, Bot bot, Calendar now) {

        for (Group group : bot.getGroups()) {
            if (!checkCosPermission(bot, group)) {
                continue;
            }
            FunctionReplyReceiver receiver = new FunctionReplyReceiver(group, log);
            if (reminderItem.getReminderMessageCodes() != null) {
                List<Message> messages = reminderMessageCodeParser.parse(receiver, reminderItem.getReminderMessageCodes());
                messages.forEach(it -> receiver.sendMessage(it));
            }
        }

    }

    private boolean checkTimeConditions(ReminderItem reminderItem, Calendar now) {
        Date date = now.getTime();
        CronExpression cronExpression = getCronExpression(reminderItem.getCron());
        return cronExpression != null && cronExpression.isSatisfiedBy(date);
    }



    


    

    private void logHourlyHeatBeat(Calendar now) {
        if (now.get(Calendar.MINUTE) == 0) {
            log.info("HourlyHeatBeat");
        }
    }

    private void hourlyChatClockArrive(Calendar now) {
        Collection<Bot> bots = Bot.getInstances();
        useReminderList(hourlyChatReminderItems, bots, now);
    }

    private void customRemiderClockArrive(Calendar now) {
        ReminderList reminderList = reminderListRepository.findSingleton();
        if (reminderList == null) {
            return;
        }
        Collection<Bot> bots = Bot.getInstances();
        boolean modidied = useReminderList(reminderList.getItems(), bots, now);
        if (modidied) {
            reminderListRepository.saveSingleton(reminderList);
        }

    }
    
    private class ReminderMessageCodeParser {
        
        public static final String IMAGE_CODE_PREFIX = "IMAGE:";
        public static final String AUDIO_CODE_PREFIX = "AUDIO:";
        
        private static final String IMAGE_FOLDER = "images/";
        public static final String AUDIO_FOLDER = "audios/";
        
        public ReminderMessageCodeParser() {
            resolveFunctionDataFile(IMAGE_FOLDER).mkdir();
            resolveFunctionDataFile(AUDIO_FOLDER).mkdir();
        }
        
        public List<Message> parse(FunctionReplyReceiver receiver, List<String> codes) {
            return codes.stream().map(it -> parse(receiver, it)).collect(Collectors.toList());
        }
        
        public Message parse(FunctionReplyReceiver receiver, String code) {
            if (code.startsWith(IMAGE_CODE_PREFIX)) {
                String fileName = code.substring(IMAGE_CODE_PREFIX.length());
                var externalResource = ExternalResource.create(resolveFunctionDataFile(IMAGE_FOLDER + fileName));
                return receiver.uploadImageAndCloseOrNotSupportPlaceholder(externalResource);
            } else if (code.startsWith(AUDIO_CODE_PREFIX)) {
                String fileName = code.substring(AUDIO_CODE_PREFIX.length());
                var externalResource = ExternalResource.create(resolveFunctionDataFile(AUDIO_FOLDER + fileName));
                return receiver.uploadImageAndCloseOrNotSupportPlaceholder(externalResource);
            } else {
                return new PlainText(code);
            }
        }
    }

    private class ReminderTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (logMinuteClockArrival) {
                    log.info("MinuteClockArrival, this = " + Integer.toHexString(hashCode()));
                }
                Calendar now = Calendar.getInstance();
                logHourlyHeatBeat(now);
                hourlyChatClockArrive(now);
                customRemiderClockArrive(now);
            } catch (Exception e) {
                log.error("ReminderTimerTask error:", e);
            }
        }



    }

}
