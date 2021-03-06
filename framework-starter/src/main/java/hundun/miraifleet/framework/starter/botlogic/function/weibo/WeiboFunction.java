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

        public CompositeCommandFunctionComponent() {
            super(plugin, botLogic, characterName, functionName);
        }

        @SubCommand("??????????????????")
        public void updateAndGetUserInfoCache(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
            listenConfig.forEach((uid, format) -> weiboService.getUserInfoCacheOptionUpdate(uid, true));
            sender.sendMessage("?????????");
            return;
        }

        @SubCommand("debugChangeTopCardCreateTime")
        public void debugChangeTopCardCreateTime(CommandSender sender, String uid) {
            if (!checkCosPermission(sender)) {
                return;
            }
            weiboService.debugChangeTopCardCreateTime(uid);
        }

        @SubCommand("??????????????????")
        public void listListen(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }
            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
            sender.sendMessage(listenConfig.toString());
        }

        @SubCommand("????????????")
        public void listTopSummary(CommandSender sender) {
            if (!checkCosPermission(sender)) {
                return;
            }

            Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();

            StringBuilder builder = new StringBuilder();
            for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                String uid= entry.getKey();
                WeiboViewFormat format = entry.getValue();

                WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, format);
                if (cardCacheAndImage != null) {
                    String summary = "?????????" + cardCacheAndImage.getWeiboCardCache().getScreenName() + "??????????????????????????????" + cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime().toString();
                    builder.append(summary).append("\n");
                }
            }
            if (builder.length() == 0) {
                sender.sendMessage("?????????????????????~");
            } else {
                sender.sendMessage(builder.toString());
            }

        }

        @SubCommand("????????????")
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
                sender.sendMessage("????????????" + name);
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

        if (newCardCacheAndImage == null) {
            group.sendMessage("?????????????????????~");
            return;
        }

        WeiboCardCache newBlog = newCardCacheAndImage.getWeiboCardCache();
        MessageChain chain = MessageUtils.newChain();

        chain = chain.plus(new PlainText("??????????????????" + newBlog.getScreenName() + " " + newBlog.getBlogCreatedDateTime().toString() + "\n\n"));

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
            builder.append("\n??????" + newCardCacheAndImage.getImageUrls().size() + "????????????");
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
