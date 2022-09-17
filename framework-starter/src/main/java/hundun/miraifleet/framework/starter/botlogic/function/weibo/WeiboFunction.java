package hundun.miraifleet.framework.starter.botlogic.function.weibo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.function.FunctionReplyReceiver;
import hundun.miraifleet.framework.helper.repository.SingletonDocumentRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboService.WeiboCardView;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboConfig;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboPushFilterFlag;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.TopCardInfoRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.WeiboUserInfoCacheRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboCardCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboUserInfoCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboApiFeignClient;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboPictureApiFeignClient;
import lombok.Data;
import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.AbstractCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

/**
 * @author hundun
 * Created on 2021/08/12
 */
public class WeiboFunction extends BaseFunction<WeiboFunction.SessionData> {

    private static final String BLOG_SCREENNAME_PLACEHOLDER = "${screenName}";
    private static final String BLOG_TIME_PLACEHOLDER = "${time}";
    public static final String DEFAULT_NONEWBLOGMESSAGETEMPLATE;
    public static final String DEFAULT_NEWBLOGMESSAGETEMPLATE;
    public static final String DEFAULT_SUMMARYBLOGMESSAGETEMPLATE;
    
    static {
        DEFAULT_NONEWBLOGMESSAGETEMPLATE = "现在还没有饼哦~";
        DEFAULT_NEWBLOGMESSAGETEMPLATE = String.format("新饼！来自：%s %s\n\n", BLOG_SCREENNAME_PLACEHOLDER, BLOG_TIME_PLACEHOLDER);
        DEFAULT_SUMMARYBLOGMESSAGETEMPLATE = String.format("来自：%s，最新的饼的时间是：%s", BLOG_SCREENNAME_PLACEHOLDER, BLOG_TIME_PLACEHOLDER);
    }
    
    private final WeiboService weiboService;
    private final SingletonDocumentRepository<WeiboConfig> configRepository;

    @Getter
    private final CompositeCommandFunctionComponent commandComponent;

    public WeiboFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName,
            @Nullable Supplier<WeiboConfig> weiboConfigDefaultDataSupplier
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "WeiboFunction",
            (() -> new WeiboFunction.SessionData())
            );

        this.weiboService = new WeiboService(
                plugin.getLogger(),
                WeiboApiFeignClient.instance(plugin.getLogger()),
                WeiboPictureApiFeignClient.instance(plugin.getLogger()),
                new WeiboUserInfoCacheRepository(plugin, resolveDataRepositoryFile("WeiboUserInfoCacheRepository.json")),
                new TopCardInfoRepository(plugin, resolveDataRepositoryFile("TopCardInfoRepository.json")),
                resolveFunctionCacheFileFolder()
                );
        this.configRepository = new SingletonDocumentRepository<>(
                plugin,
                resolveFunctionConfigFile("WeiboConfig.json"),
                WeiboConfig.class,
                weiboConfigDefaultDataSupplier
                );
        botLogic.getPluginScheduler().repeating(5 * 60 * 1000, new WeiboTask());
        this.commandComponent = new CompositeCommandFunctionComponent();
        handleVersionChange();
    }

    private void handleVersionChange() {
        WeiboConfig weiboConfig = configRepository.findSingleton();
        boolean dirty = false;
        
        if (weiboConfig.getNoNewBlogMessageTemplate() == null) {
            dirty = true;
            weiboConfig.setNoNewBlogMessageTemplate(DEFAULT_NONEWBLOGMESSAGETEMPLATE);
        }
        if (weiboConfig.getNewBlogMessageTemplate() == null) {
            dirty = true;
            weiboConfig.setNewBlogMessageTemplate(DEFAULT_NEWBLOGMESSAGETEMPLATE);
        }
        if (weiboConfig.getSummaryBlogMessageTemplate() == null) {
            dirty = true;
            weiboConfig.setSummaryBlogMessageTemplate(DEFAULT_SUMMARYBLOGMESSAGETEMPLATE);
        }
        
        if (dirty) {
            log.info("weiboConfig update for new version.");
            configRepository.saveSingleton(weiboConfig);
        }
    }

    @Data
    public static class SessionData {
        LocalDateTime taskLastCheckTime = LocalDateTime.now();

    }

    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }
    
    @Override
    public AbstractCommand provideDebugCommand() {
        return new DebugCompositeCommandFunctionComponent();
    }

    public class DebugCompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {

        public DebugCompositeCommandFunctionComponent() {
            super(plugin, botLogic, new DebugLevelFunctionComponentConstructPack(characterName, functionName));
        }
        
        @SubCommand("debugChangeTopCardCreateTime")
        public void debugChangeTopCardCreateTime(CommandSender sender, String uid) {
            if (!checkCosPermission(sender)) {
                return;
            }
            weiboService.debugChangeTopCardCreateTime(uid);
        }
        
    }

    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {

        public CompositeCommandFunctionComponent() {
            super(plugin, botLogic, characterName, functionName);
        }

        @SubCommand("刷新微博订阅")
        public void updateAndGetUserInfoCache(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            WeiboConfig weiboConfig = configRepository.findSingleton();
            Map<String, WeiboViewFormat> listenConfig = weiboConfig.getListenConfig();
            listenConfig.forEach((uid, format) -> weiboService.getUserInfoCacheOptionUpdate(uid, true));
            sender.sendMessage("已刷新");
            return;
        }

        

        @SubCommand("查询微博订阅")
        public void listListen(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            WeiboConfig weiboConfig = configRepository.findSingleton();
            Map<String, WeiboViewFormat> listenConfig = weiboConfig.getListenConfig();
            sender.sendMessage(listenConfig.toString());
        }

        @SubCommand("最新微博")
        public void listTopSummary(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }

            WeiboConfig weiboConfig = configRepository.findSingleton();
            Map<String, WeiboViewFormat> listenConfig = weiboConfig.getListenConfig();
            
            StringBuilder builder = new StringBuilder();
            for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                String uid= entry.getKey();
                WeiboViewFormat format = entry.getValue();

                WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, format);
                if (cardCacheAndImage != null) {
                    String summary = weiboConfig.getSummaryBlogMessageTemplate()
                            .replace(BLOG_SCREENNAME_PLACEHOLDER, cardCacheAndImage.getWeiboCardCache().getScreenName())
                            .replace(BLOG_TIME_PLACEHOLDER, cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime().toString())
                            ;
                    builder.append(summary).append("\n");
                }
            }
            if (builder.length() == 0) {
                sender.sendMessage(weiboConfig.getNoNewBlogMessageTemplate());
            } else {
                sender.sendMessage(builder.toString());
            }

        }

        @SubCommand("最新微博")
        public void listTopForUid(CommandSender sender, String name) {
            if (!checkCosPermission(sender)) {
                return;
            }
            WeiboConfig weiboConfig = configRepository.findSingleton();
            Map<String, WeiboViewFormat> listenConfig = weiboConfig.getListenConfig();

            String targetUid = "";
            for (String uid : listenConfig.keySet()) {
                WeiboUserInfoCache userInfoCache = weiboService.getUserInfoCacheOptionUpdate(uid, false);
                if (userInfoCache != null && userInfoCache.getScreenName().equals(name)) {
                    targetUid = userInfoCache.getUid();
                }
            }

            WeiboViewFormat format = listenConfig.get(targetUid);
            if (format == null) {
                sender.sendMessage("未订阅：" + name);
            } else {
                WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(targetUid, format);

                sendBlogToBot(cardCacheAndImage, new FunctionReplyReceiver(sender, log));
            }
        }

    }




//    public void latestWeibo(CommandSender sender, String uid) {
//        File cacheFolder = resolveFunctionCacheFileFolder();
//        WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, cacheFolder, WeiboViewFormat.NO_IMAGE);
//        sendBlogToBot(cardCacheAndImage, new CommandReplyReceiver(sender, plugin.getLogger()));
//        return;
//    }



    private void sendBlogToBot(WeiboCardView newCardCacheAndImage, FunctionReplyReceiver group) {
        WeiboConfig weiboConfig = configRepository.findSingleton();
        if (newCardCacheAndImage == null) {
            group.sendMessage(weiboConfig.getNoNewBlogMessageTemplate());
            return;
        }

        WeiboCardCache newBlog = newCardCacheAndImage.getWeiboCardCache();
        MessageChain chain = MessageUtils.newChain();
        
        
        String text = weiboConfig.getNewBlogMessageTemplate()
                .replace(BLOG_SCREENNAME_PLACEHOLDER, newBlog.getScreenName())
                .replace(BLOG_TIME_PLACEHOLDER, newBlog.getBlogCreatedDateTime().toString())
                ;
        chain = chain.plus(new PlainText(text));

        if (newBlog.getBlogTextDetail() != null) {
            chain = chain.plus(new PlainText(newBlog.getBlogTextDetail()));
        }

        for (File imageFile : newCardCacheAndImage.getImages()) {
            Image image = group.uploadImageAndClose(ExternalResource.create(imageFile));
            if (image != null) {
                chain = chain.plus(image);
            }
        }

        if (!newCardCacheAndImage.getImageUrls().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n以及" + newCardCacheAndImage.getImageUrls().size() + "张图片。");
            chain = chain.plus(new PlainText(builder.toString()));
        }

        group.sendMessage(chain);
    }







    private Map<String, List<WeiboPushFilterFlag>> getPushFilterFlagsOrEmpty() {
        WeiboConfig weiboConfig = configRepository.findSingleton();
        if (weiboConfig == null) {
            plugin.getLogger().warning("weiboConfig is null");
            return new HashMap<>(0);
        }
        return weiboConfig.getPushFilterFlags();
    }




    private class WeiboTask extends TimerTask {

        @Override
        public void run() {
            timerClockArrive();
        }

        private void timerClockArrive() {
            SessionData sessionData = getOrCreateSessionData();
            WeiboConfig weiboConfig = configRepository.findSingleton();
            plugin.getLogger().info("checkNewBlog Scheduled arrival, LastCheckTime = " + sessionData.getTaskLastCheckTime().toString());
            Collection<Bot> bots = Bot.getInstances();
            for (Bot bot: bots) {

                //log.info("checkGroupListen called");
                try {
                    Map<String, WeiboViewFormat> listenConfig = weiboConfig.getListenConfig();
                    for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                        String uid= entry.getKey();
                        WeiboViewFormat format = entry.getValue();

                        WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, format);
                        if (cardCacheAndImage == null) {
                            continue;
                        }

                        boolean filtered = checkPushFilter(cardCacheAndImage);
                        boolean isNew = cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime().isAfter(sessionData.getTaskLastCheckTime());
                        if (isNew && !filtered) {
                            plugin.getLogger().info("uid = " + uid + " has new weibo: " + cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime());
                            for (Group group : bot.getGroups()) {
                                if (!checkCosPermission(bot, group)) {
                                    continue;
                                }
                                sendBlogToBot(cardCacheAndImage, new FunctionReplyReceiver(group, log));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("checkNewBlog Scheduled error: ", e);
                }
            }

            sessionData.setTaskLastCheckTime(LocalDateTime.now());



        }

    }

    public boolean checkPushFilter(WeiboCardView cardCacheAndImage) {
        Map<String, List<WeiboPushFilterFlag>> pushFilter = getPushFilterFlagsOrEmpty();
        List<WeiboPushFilterFlag> filterFlags = pushFilter.get(cardCacheAndImage.getWeiboCardCache().getUid());
        if (filterFlags == null) {
            return false;
        }
        if (filterFlags.contains(WeiboPushFilterFlag.RETWEET)) {
            if (cardCacheAndImage.getWeiboCardCache().isRetweeted()) {
                return true;
            }
        }
        return false;
    }


}
