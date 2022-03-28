package hundun.miraifleet.framework.starter.botlogic.function.weibo;

import java.io.File;
import java.io.IOException;
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
import hundun.miraifleet.framework.core.helper.repository.SingletonDocumentRepository;
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
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
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

    private final WeiboService weiboService;

    private final SingletonDocumentRepository<WeiboConfig> configRepository;

    @Getter
    private final CompositeCommandFunctionComponent commandComponent;

    @Deprecated
    public WeiboFunction(
            BaseBotLogic baseBotLogic,
            JavaPlugin plugin,
            String characterName,
            boolean skipRegisterCommand
            ) {
        this(baseBotLogic, plugin, characterName, skipRegisterCommand, null);
    }

    public WeiboFunction(
            BaseBotLogic baseBotLogic,
            JavaPlugin plugin,
            String characterName,
            boolean skipRegisterCommand,
            @Nullable Supplier<Map<String, WeiboConfig>> weiboConfigDefaultDataSupplier
            ) {
        super(
            baseBotLogic,
            plugin,
            characterName,
            "WeiboFunction",
            skipRegisterCommand,
            (() -> new WeiboFunction.SessionData())
            );

        this.weiboService = new WeiboService(
                plugin.getLogger(),
                WeiboApiFeignClient.instance(plugin.getLogger()),
                WeiboPictureApiFeignClient.instance(plugin.getLogger()),
                new WeiboUserInfoCacheRepository(plugin, resolveFunctionRepositoryFile("WeiboUserInfoCacheRepository.json")),
                new TopCardInfoRepository(plugin, resolveFunctionRepositoryFile("TopCardInfoRepository.json"))
                );
        this.configRepository = new SingletonDocumentRepository<>(
                plugin,
                resolveFunctionConfigFile("WeiboConfig.json"),
                WeiboConfig.class,
                weiboConfigDefaultDataSupplier
                );
        plugin.getScheduler().repeating(5 * 60 * 1000, new WeiboTask());
        this.commandComponent = new CompositeCommandFunctionComponent(plugin, characterName, functionName);
    }


    @Data
    public static class SessionData {
        LocalDateTime taskLastCheckTime = LocalDateTime.now();

    }

    @Override
    public AbstractCommand provideCommand() {
        return commandComponent;
    }


    public class CompositeCommandFunctionComponent extends AbstractCompositeCommandFunctionComponent {

        public CompositeCommandFunctionComponent(JvmPlugin plugin, String characterName, String functionName) {
            super(plugin, characterName, functionName);
        }

        @SubCommand("刷新微博订阅")
        public void updateAndGetUserInfoCache(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
            listenConfig.forEach((uid, format) -> weiboService.getUserInfoCacheOptionUpdate(uid, true));
            sender.sendMessage("已刷新");
            return;
        }

        @SubCommand("debugChangeTopCardCreateTime")
        public void debugChangeTopCardCreateTime(CommandSender sender, String uid) {
            if (!checkCosPermission(sender)) {
                return;
            }
            weiboService.debugChangeTopCardCreateTime(uid);
        }

        @SubCommand("查询微博订阅")
        public void listListen(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
            sender.sendMessage(listenConfig.toString());
        }

        @SubCommand("最新微博")
        public void listTopSummary(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }

            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();

            StringBuilder builder = new StringBuilder();
            for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                String uid= entry.getKey();
                WeiboViewFormat format = entry.getValue();

                File cacheFolder = resolveFunctionCacheFileFolder();
                WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, cacheFolder, format);
                if (cardCacheAndImage != null) {
                    String summary = "来自：" + cardCacheAndImage.getWeiboCardCache().getScreenName() + "，最新的饼的时间是：" + cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime().toString();
                    builder.append(summary).append("\n");
                }
            }
            if (builder.length() == 0) {
                sender.sendMessage("现在还没有饼哦~");
            } else {
                sender.sendMessage(builder.toString());
            }

        }

        @SubCommand("最新微博")
        public void listTopForUid(CommandSender sender, String name) {
            if (!checkCosPermission(sender)) {
                return;
            }

            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();

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
                File cacheFolder = resolveFunctionCacheFileFolder();
                WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(targetUid, cacheFolder, format);

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

        if (newCardCacheAndImage == null) {
            group.sendMessage("现在还没有饼哦~");
            return;
        }

        WeiboCardCache newBlog = newCardCacheAndImage.getWeiboCardCache();
        MessageChain chain = MessageUtils.newChain();

        chain = chain.plus(new PlainText("新饼！来自：" + newBlog.getScreenName() + " " + newBlog.getBlogCreatedDateTime().toString() + "\n\n"));

        if (newBlog.getBlogTextDetail() != null) {
            chain = chain.plus(new PlainText(newBlog.getBlogTextDetail()));
        }

        for (File imageFile : newCardCacheAndImage.getImages()) {
            ExternalResource externalResource = ExternalResource.create(imageFile);
            Image image = group.uploadImage(externalResource);
            try {
                externalResource.close();
            } catch (IOException e) {
                log.error(e);
            }
            if (image != null) {
                chain = chain.plus(image);
            }
        }

        if (!newCardCacheAndImage.getImageUrls().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n以及" + newCardCacheAndImage.getImageUrls().size() + "张图片。");
//            for (String url : newBlog.getPicsLargeUrls()) {
//                builder.append(url).append("\n");
//            }
            chain = chain.plus(new PlainText(builder.toString()));
        }

        group.sendMessage(chain);
    }





    private Map<String, WeiboViewFormat> getListenConfigOrEmpty() {
        WeiboConfig weiboConfig = configRepository.findSingleton();
        if (weiboConfig == null) {
            plugin.getLogger().warning("weiboConfig is null");
            return new HashMap<>(0);
        }
        return weiboConfig.getListenConfig();
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
            plugin.getLogger().info("checkNewBlog Scheduled arrival, LastCheckTime = " + sessionData.getTaskLastCheckTime().toString());
            Collection<Bot> bots = Bot.getInstances();
            for (Bot bot: bots) {

                //log.info("checkGroupListen called");
                try {
                    Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
                    for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                        String uid= entry.getKey();
                        WeiboViewFormat format = entry.getValue();

                        File cacheFolder = resolveFunctionCacheFileFolder();
                        WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, cacheFolder, format);
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
