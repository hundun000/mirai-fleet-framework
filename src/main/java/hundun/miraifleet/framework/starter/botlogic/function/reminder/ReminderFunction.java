package hundun.miraifleet.framework.starter.botlogic.function.reminder;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.function.FunctionReplyReceiver;
import hundun.miraifleet.framework.core.helper.repository.PluginConfigRepository;
import hundun.miraifleet.framework.core.helper.repository.PluginDataRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.config.HourlyChatConfig;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.db.ReminderListRepository;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderItem;
import hundun.miraifleet.framework.starter.botlogic.function.reminder.domain.ReminderList;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboService.WeiboCardView;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;
import lombok.extern.apachecommons.CommonsLog;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.permission.PermissionId;
import net.mamoe.mirai.console.permission.PermissionRegistryConflictException;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.permission.AbstractPermitteeId.ExactGroup;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;

/**
 * @author hundun
 * Created on 2021/08/13
 */
@AsCommand
public class ReminderFunction extends BaseFunction<Void> {

    ReminderListRepository reminderListRepository;
    private PluginConfigRepository<HourlyChatConfig> configRepository;
    List<ReminderItem> hourlyChatReminderItems = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    
    public ReminderFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "ReminderFunction",
            null
            );
        this.reminderListRepository = new ReminderListRepository(plugin, resolveFunctionRepositoryFile("ReminderListRepository.json"));
        this.configRepository = new PluginConfigRepository<>(plugin, resolveFunctionConfigFile("HourlyChatConfig.json"), HourlyChatConfig.class);
        this.scheduler.scheduleAtFixedRate(new ReminderTimerTask(), 1, 1, TimeUnit.MINUTES);
        initHourlyChatConfigToReminderItems();
    }
    
    
    @SubCommand("查询报时")
    public void listHourlyChatConfig(CommandSender sender) {
        if (!checkCosPermission(sender)) {
            return;
        }
        HourlyChatConfig config = configRepository.findSingleton();
        sender.sendMessage(config.toString());
    }


    @SubCommand("debugTimerCallReminderItem")
    public void debugTimerCallReminderItem(CommandSender sender, int fakeHour) {
        if (!checkCosPermission(sender)) {
            return;
        }
        LocalDateTime fakeNow = LocalDateTime.of(2000, 1, 1, fakeHour, 0);
        for (ReminderItem reminderItem : hourlyChatReminderItems) {
            Bot bot = sender.getBot();
            useReminderItem(reminderItem, bot, sender.getSubject(), fakeNow);
        }
    }
    
    





    private void initHourlyChatConfigToReminderItems() {
        HourlyChatConfig config = configRepository.findSingleton();
        if (config != null) {
            config.getChatTexts().forEach((hour, text) -> {
                int hourCondition = Integer.valueOf(hour);
                ReminderItem item = createReminderItem(-1, -1, -1, hourCondition, 0, -1, text, -1, -1);
                hourlyChatReminderItems.add(item);
            });
        }
    }
    
    
    
    private void useReminderItem(ReminderItem reminderItem, Bot bot, Contact contact, LocalDateTime now) {
        if (!checkTimeConditions(reminderItem, now)) {
            return;
        }
        contact.sendMessage(reminderItem.getText());
        if (reminderItem.getCount() != -1) {
            reminderItem.setCount(reminderItem.getCount() - 1);
        }
    }
    
    private boolean checkTimeConditions(ReminderItem task, LocalDateTime now) {
        
        if (task.getMonthCondition() != -1 && task.getMonthCondition() != now.getMonth().getValue()) {
            return false;
        }
        if (task.getDayOfMonthCondition() != -1 && task.getDayOfMonthCondition() != now.getDayOfMonth()) {
            return false;
        }
        if (task.getDayOfWeekCondition() != -1 && task.getDayOfWeekCondition() != now.getDayOfWeek().getValue()) {
            return false;
        }
        if (task.getHourCondition() != -1 && task.getHourCondition() != now.getHour()) {
            return false;
        }
        if (task.getMinuteCondition() != -1 && task.getMinuteCondition() != now.getMinute()) {
            return false;
        }
        return true;
    }
    
    
    
    
    
    
    private ReminderItem createReminderItem(
            int monthCondition,
            int dayOfMonthCondition,
            int dayOfWeekCondition,
            int hourCondition,
            int minuteCondition,
            int count,
            String text,
            long targetGroup,
            long botId
            ) {
        ReminderItem task = new ReminderItem();
        try {
            task.setMonthCondition(monthCondition);
            task.setDayOfMonthCondition(dayOfMonthCondition);
            task.setDayOfWeekCondition(dayOfWeekCondition);
            task.setHourCondition(hourCondition);
            task.setMinuteCondition(minuteCondition);
            task.setCount(count);
            task.setText(text);
            task.setTargetGroup(targetGroup);
        } catch (Exception e) {
            log.error("createReminderItem error:", e);
            return null;
        }
        
        return task;
    }
    
    private class ReminderTimerTask extends TimerTask {

        
        @Override
        public void run() {
            try {
                LocalDateTime now = LocalDateTime.now();
                logHourlyHeatBeat(now);
                hourlyChatClockArrive(now);
                customRemiderClockArrive(now);
            } catch (Exception e) {
                log.error("ReminderTimerTask error:", e);
            }
        }
        
        private void logHourlyHeatBeat(LocalDateTime now) {
            if (now.getMinute() == 0) {
                log.info("HourlyHeatBeat");
            }
        }
        
        private void hourlyChatClockArrive(LocalDateTime now) {
            for (ReminderItem reminderItem : hourlyChatReminderItems) {
                Collection<Bot> bots = Bot.getInstances();
                for (Bot bot: bots) {
                    for (Group group : bot.getGroups()) {
                        if (!checkCosPermission(bot, group)) {
                            continue;
                        }
                        useReminderItem(reminderItem, bot, group, now);
                    }
                }
            }
        }
        
        private void customRemiderClockArrive(LocalDateTime now) {
            Collection<Bot> bots = Bot.getInstances();
            for (Bot bot: bots) {
                ReminderList reminderList = reminderListRepository.findById(Long.toString(bot.getId()));
                if (reminderList == null) {
                    continue;
                }
                for (ReminderItem reminderItem : reminderList.getItems()) {
                    Group group = bot.getGroup(reminderItem.getTargetGroup());
                    if (!checkCosPermission(bot, group)) {
                        continue;
                    }
                    useReminderItem(reminderItem, bot, group, now);
                    if (reminderItem.getCount() == 0) {
                        reminderListRepository.delete(reminderList);
                    } else {
                        reminderListRepository.save(reminderList);
                    }
                }
            }
        }
        
    }

}
