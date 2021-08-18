package hundun.miraifleet.framework.starter.botlogic.function.weibo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import hundun.miraifleet.framework.core.function.AsCommand;
import hundun.miraifleet.framework.core.function.AsListenerHost;
import hundun.miraifleet.framework.core.function.BaseFunction;
import hundun.miraifleet.framework.core.function.CommandReplyReceiver;
import hundun.miraifleet.framework.core.helper.feign.FeignClientFactory;
import hundun.miraifleet.framework.core.helper.repository.PluginConfigRepository;
import hundun.miraifleet.framework.core.helper.repository.PluginDataRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.WeiboService.WeiboCardView;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboConfig;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.TopCardInfoRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.WeiboUserInfoCacheRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboCardCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboUserInfoCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboApiFeignClient;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboPictureApiFeignClient;
import lombok.Data;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.MemberCommandSender;
import net.mamoe.mirai.console.permission.PermissionService;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

/**
 * @author hundun
 * Created on 2021/08/12
 */
@AsCommand
public class WeiboFunction extends BaseFunction<WeiboFunction.SessionData> {
    
    private final WeiboService weiboService;
    
    private final PluginConfigRepository<WeiboConfig> configRepository;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public WeiboFunction(
            BaseBotLogic baseBotLogic,
            JvmPlugin plugin,
            String characterName
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
                new WeiboUserInfoCacheRepository(plugin, resolveFunctionRepositoryFile("WeiboUserInfoCacheRepository.json")), 
                new TopCardInfoRepository(plugin, resolveFunctionRepositoryFile("TopCardInfoRepository.json"))
                );
        this.configRepository = new PluginConfigRepository<>(plugin, resolveFunctionConfigFile("WeiboConfig.json"), WeiboConfig.class);
        this.scheduler.scheduleAtFixedRate(new WeiboTask(), 1, 5, TimeUnit.MINUTES);
    }
    

    @Data
    public static class SessionData {
        LocalDateTime taskLastCheckTime = LocalDateTime.now();
        
    }
    
    @SubCommand("刷新微博用户数据")
    public void updateAndGetUserInfoCache(CommandSender sender) {

        Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
        listenConfig.forEach((uid, format) -> weiboService.updateAndGetUserInfoCache(uid, true));
        sender.sendMessage("已刷新");
        return;
    }
    
    
//    public void latestWeibo(CommandSender sender, String uid) {
//        File cacheFolder = resolveFunctionCacheFileFolder();
//        WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, cacheFolder, WeiboViewFormat.NO_IMAGE);
//        sendBlogToBot(cardCacheAndImage, new CommandReplyReceiver(sender, plugin.getLogger()));
//        return;
//    }


    
    private void timerSendBlogToBot(WeiboCardView newCardCacheAndImage, Group group) {
        
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
    
    @SubCommand("debugChangeTopCardCreateTime")
    public void debugChangeTopCardCreateTime(CommandSender sender, String uid) {
        weiboService.debugChangeTopCardCreateTime(uid);
    }
    
    @SubCommand("debugListListen")
    public void debugListListen(CommandSender sender) {
        Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
        sender.sendMessage(listenConfig.toString());
    }
    
    private Map<String, WeiboViewFormat> getListenConfigOrEmpty() {
        WeiboConfig weiboConfig = configRepository.findSingleton();
        if (weiboConfig == null) {
            plugin.getLogger().warning("weiboConfig is null");
            return new HashMap<>(0);
        }
        return weiboConfig.getListenConfig();
    }
    

    
    
    
    
    private class WeiboTask extends TimerTask {

        @Override
        public void run() {
            timerClockArrive();
        }
        
        private void timerClockArrive() {
            plugin.getLogger().info("checkNewBlog Scheduled arrival");
            Collection<Bot> bots = Bot.getInstances();
            for (Bot bot: bots) {
                for (Group group : bot.getGroups()) {
                    log.info("checkGroupListen called");
                    try {
                        
                        SessionData sessionData = getOrCreateSessionData();

                        Map<String, WeiboViewFormat> listenConfig = getListenConfigOrEmpty();
                        for (Entry<String, WeiboViewFormat> entry : listenConfig.entrySet()) {
                            String uid= entry.getKey();
                            WeiboViewFormat format = entry.getValue();

                            File cacheFolder = resolveFunctionCacheFileFolder();
                            WeiboCardView cardCacheAndImage = weiboService.updateAndGetTopBlog(uid, cacheFolder, format);
                            if (cardCacheAndImage == null) {
                                continue;
                            }
                            boolean isNew = cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime().isAfter(sessionData.getTaskLastCheckTime());
                            if (isNew) {
                                plugin.getLogger().info("uid = " + uid + " has new weibo: " + cardCacheAndImage.getWeiboCardCache().getBlogCreatedDateTime());
                                timerSendBlogToBot(cardCacheAndImage, group);
                            }
                        }
                        
                        sessionData.setTaskLastCheckTime(LocalDateTime.now());
                    } catch (Exception e) {
                        log.error("checkNewBlog Scheduled error: ", e);
                    }
                }
            }
            SessionData sessionData = getOrCreateSessionData();
            sessionData.setTaskLastCheckTime(LocalDateTime.now());

            
            
        }

    }
}
