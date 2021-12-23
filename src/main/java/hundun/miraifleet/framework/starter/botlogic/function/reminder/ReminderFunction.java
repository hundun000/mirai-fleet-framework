package hundun.miraifleet.framework.starter.botlogic.function.reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.helper.repository.SingletonDocumentRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.config.HourlyChatConfig;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderItem;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderList;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboConfig;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@AsCommand
public class ReminderFunction extends BaseFunction<Void> {

    SingletonDocumentRepository<ReminderList> reminderListRepository;
    private SingletonDocumentRepository<HourlyChatConfig> configRepository;
    List<ReminderItem> hourlyChatReminderItems = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Map<String, CronExpression> cronExpressionCaches = new HashMap<>();
    
    @Deprecated
    public ReminderFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        this(baseBotLogic, plugin, characterName, null, null);
    }
    
    public ReminderFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            @Nullable Supplier<Map<String, ReminderList>> reminderListDefaultDataSupplier,
            @Nullable Supplier<Map<String, HourlyChatConfig>> hourlyChatConfigDefaultDataSupplier 
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "ReminderFunction",
            null
            );
        this.reminderListRepository = new SingletonDocumentRepository<>(plugin, resolveFunctionRepositoryFile("ReminderListRepository.json"), ReminderList.class, reminderListDefaultDataSupplier);
        this.configRepository = new SingletonDocumentRepository<>(plugin, resolveFunctionConfigFile("HourlyChatConfig.json"), HourlyChatConfig.class, hourlyChatConfigDefaultDataSupplier);
        this.scheduler.scheduleAtFixedRate(new ReminderTimerTask(), 1, 1, TimeUnit.MINUTES);
        initHourlyChatConfigToReminderItems();
    }
    
    
    @SubCommand("查询报时")
    public void listHourlyChatConfig(CommandSender sender) {
        if (!checkCosPermission(sender)) {
            return;
        }
        sender.sendMessage(itemsToText(hourlyChatReminderItems));
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
        reminderList.getItems().add(createByCommand(cornRawFomat, text, count));
        reminderListRepository.saveSingleton(reminderList);
        sender.sendMessage("OK");
    }
    
    
    private String itemsToText(List<ReminderItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("items:\n");
        for (int i = 0; i < items.size(); i++) {
            ReminderItem item = items.get(i);
            builder.append("id:").append(i).append("\t");
            builder.append(item.toString()).append("\n");
        }
        return builder.toString();
    }


    @SubCommand("debugTimerCallReminderItem")
    public void debugTimerCallReminderItem(CommandSender sender, String timeString) {
        if (!checkCosPermission(sender)) {
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
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        
        logHourlyHeatBeat(calendar);
        hourlyChatClockArrive(calendar);
        customRemiderClockArrive(calendar);
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



    private void initHourlyChatConfigToReminderItems() {
        HourlyChatConfig config = configRepository.findSingleton();
        if (config != null) {
            config.getChatTexts().forEach((hour, text) -> {
                int hourCondition = Integer.valueOf(hour);
                ReminderItem item = createByHourlyChatCron(hourCondition, text);
                hourlyChatReminderItems.add(item);
            });
        }
    }
    
    private boolean useReminderList(List<ReminderItem> items, Collection<Bot> bots, Calendar now) {
        Iterator<ReminderItem> iterator = items.iterator();
        boolean modified = false;
        while (iterator.hasNext()) {
            ReminderItem reminderItem = iterator.next();
            if (!checkTimeConditions(reminderItem, now)) {
                continue;
            }
            if (reminderItem.getCount() != null && reminderItem.getCount() == 0) {
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
            group.sendMessage(reminderItem.getText());
        }

    }
    
    private boolean checkTimeConditions(ReminderItem task, Calendar now) {
        Date date = now.getTime();
        CronExpression cronExpression = getCronExpression(task.getCron());
        return cronExpression != null && cronExpression.isSatisfiedBy(date);
    }
    
    
    
    private ReminderItem createByCommand(
            String cornRawFomat,
            String text,
            Integer count
            ) {
        ReminderItem task = new ReminderItem();
        try {
            String cron = cornRawFomat.replace("~", " ");
            task.setCron(cron);
            task.setCount(count);
            task.setText(text);
        } catch (Exception e) {
            log.error("createReminderItem error:", e);
            return null;
        }
        
        return task;
    }
    
    
    private ReminderItem createByHourlyChatCron(
            int hourCondition,
            String text
            ) {
        ReminderItem task = new ReminderItem();
        try {
            String cron = "* 0 " + hourCondition + " * * ?";
            task.setCron(cron);
            task.setCount(null);
            task.setText(text);
        } catch (Exception e) {
            log.error("createReminderItem error:", e);
            return null;
        }
        
        return task;
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
    
    private class ReminderTimerTask extends TimerTask {

        
        @Override
        public void run() {
            try {
                Calendar now = GregorianCalendar.getInstance();
                logHourlyHeatBeat(now);
                hourlyChatClockArrive(now);
                customRemiderClockArrive(now);
            } catch (Exception e) {
                log.error("ReminderTimerTask error:", e);
            }
        }
        
        
        
    }

}
