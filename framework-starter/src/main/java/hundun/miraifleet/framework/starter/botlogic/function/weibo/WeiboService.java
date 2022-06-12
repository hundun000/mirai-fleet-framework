package hundun.miraifleet.framework.starter.botlogic.function.weibo;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import hundun.miraifleet.framework.helper.file.CacheableFileHelper;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.config.WeiboViewFormat;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.TopCardInfoRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.db.WeiboUserInfoCacheRepository;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.TopCardInfo;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboCardCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.domain.WeiboUserInfoCache;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboApiFeignClient;
import hundun.miraifleet.framework.starter.botlogic.function.weibo.feign.WeiboPictureApiFeignClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/04/23
 */
public class WeiboService {

    ObjectMapper mapper = new ObjectMapper();

    CacheableFileHelper cacheableFileHelper;
    
    WeiboApiFeignClient weiboApiFeignClient;

    WeiboPictureApiFeignClient weiboPictureApiFeignClient;

    WeiboUserInfoCacheRepository userInfoCacheRepository;

    TopCardInfoRepository topCardInfoRepository;


    //public static final String yjUid = "6279793937";
    //public static final String CHOSSHANLAND_UID = "6441489862";
    MiraiLogger log;

    String API_TYPE_PARAM = "uid";

    private final Function<String, InputStream> uncachedWeiboImageProvider;

    public WeiboService(
            MiraiLogger miraiLogger,
            WeiboApiFeignClient weiboApiFeignClient,
            WeiboPictureApiFeignClient weiboPictureApiFeignClient,
            WeiboUserInfoCacheRepository userInfoCacheRepository,
            TopCardInfoRepository topCardInfoRepository, 
            File rootCacheFolder
            ) {
        this.weiboApiFeignClient = weiboApiFeignClient;
        this.weiboPictureApiFeignClient = weiboPictureApiFeignClient;
        this.userInfoCacheRepository = userInfoCacheRepository;
        this.topCardInfoRepository = topCardInfoRepository;

        this.cacheableFileHelper = new CacheableFileHelper(rootCacheFolder, "weiboImage", miraiLogger);
        this.log = miraiLogger;
        this.uncachedWeiboImageProvider = fileId -> downloadUncachedWeiboImage(fileId);
    }



    private void updateBlogDetail(WeiboCardCache cardCache) {

        if (cardCache.getBlogTextDetail() == null) {

            log.debug("updateBlogDetail get response.");
            try {
                //String responseString = weiboApiFeignClient.blogDetail(cardCache.getMblog_id());
                //JsonNode responseJson = mapper.readTree(responseString);
                JsonNode responseJson = weiboApiFeignClient.blogDetail(cardCache.getBlogId());

                String longTextContent = responseJson.get("data").get("longTextContent").asText();

                String detailText = formatBlogDetail(longTextContent);
                cardCache.setBlogTextDetail(detailText);


                //cardCacheRepository.save(cardCache);
                log.debug("updateBlogDetail success: " + detailText.substring(0, Math.min(20, detailText.length())));
            } catch (Exception e) {
                log.error("updateBlogDetail error: ", e);
                log.error("updateBlogDetail error cardCache = " + cardCache);
                cardCache.setBlogTextDetail("（查询全文失败）\n" + cardCache.getBlogTextDetail());
            }
        }

    }



    private static String formatBlogDetail(String text) {
        String detailText = text.replace("<br />", "\n");
        while (detailText.contains("<a") && detailText.contains("/a>")) {
            int start = detailText.indexOf("<a");
            int end = detailText.indexOf("/a>") + "/a>".length();

            detailText = detailText.substring(0, start) + detailText.substring(end, detailText.length());
        }
        return detailText;
    }

    public WeiboUserInfoCache getUserInfoCacheOptionUpdate(String uid, boolean forceUpdate) {

        if (userInfoCacheRepository.existsById(uid) && !forceUpdate) {
            return userInfoCacheRepository.findById(uid);
        }

        try {
            //String responseString = weiboApiFeignClient.get(uid, API_TYPE_PARAM, uid, null);
            //JsonNode responseJson = mapper.readTree(responseString);
            JsonNode responseJson = weiboApiFeignClient.get(uid, API_TYPE_PARAM, uid, null);
            JsonNode tabsNode = responseJson.get("data").get("tabsInfo").get("tabs");
            if (tabsNode.isArray()) {
                boolean updated = false;
                WeiboUserInfoCache userInfoCacahe;
                if (userInfoCacheRepository.existsById(uid)) {
                    userInfoCacahe = userInfoCacheRepository.findById(uid);
                } else {
                    userInfoCacahe = new WeiboUserInfoCache();
                    userInfoCacahe.setUid(uid);
                    String screen_name = responseJson.get("data").get("userInfo").get("screen_name").asText();
                    userInfoCacahe.setScreenName(screen_name);
                    updated = true;
                }

                for (final JsonNode tabNode : tabsNode) {
                    if (tabNode.get("tabKey").asText().equals("weibo")) {
                        String newContainerid = tabNode.get("containerid").asText();
                        if (userInfoCacahe.getWeiboContainerid() == null || !userInfoCacahe.getWeiboContainerid().equals(newContainerid)) {
                            userInfoCacahe.setWeiboContainerid(newContainerid);
                            updated = true;
                        }
                        break;
                    }
                }

                if (updated) {
                    userInfoCacheRepository.save(userInfoCacahe);
                    log.debug("userInfoCacahe updated: " + userInfoCacahe);
                } else {
                    log.debug("userInfoCacahe is up-to-date: " + uid);
                }
                return userInfoCacahe;
            } else {
                log.warning("tabsNode not array, responseJson = " + responseJson);
            }

        } catch (Exception e) {
            log.error("updateContainerid :", e);
            log.error("updateContainerid error uid = " + uid);
        }
        return null;
    }

    public TopCardInfo getTopInfo(String uid) {
//        WeiboUserInfoCache userInfoCacahe = userInfoCacheRepository.findById(uid);
//        if (userInfoCacahe == null) {
//            return null;
//        }
        TopCardInfo topCardInfo = topCardInfoRepository.findById(uid);
//        if (topCardInfo != null) {
//            WeiboCardCache cardCache = topCardInfo.getCardCache();
//            return "来自：" + cardCache.getScreenName() + "，最新的饼的时间是：" + cardCache.getBlogCreatedDateTime().toString();
//        }

        return topCardInfo;
    }

//    // Sat Apr 10 11:16:34 +0800 2021
//    //
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
//
//    public static void main(String[] args) {
//        ZonedDateTime now = ZonedDateTime.now();
//        System.out.println(dateTimeFormatter.format(now));
//        ZonedDateTime localDateTime = ZonedDateTime.parse("Sat Apr 10 11:16:34 +0800 2021", dateTimeFormatter);
//        System.out.println(localDateTime.toString());
//    }

    @AllArgsConstructor
    @Data
    public class WeiboCardView {
        WeiboCardCache weiboCardCache;
        @ToString.Exclude
        List<File> images;
        @ToString.Exclude
        List<String> imageUrls;
    }

    public void debugChangeTopCardCreateTime(String uid) {
        TopCardInfo topCardInfo = topCardInfoRepository.findById(uid);
        if (topCardInfo != null) {
            topCardInfo.getCardCache().setBlogCreatedDateTime(LocalDateTime.now());
            topCardInfoRepository.save(topCardInfo);
        } else {
            log.warning("debugChangeTopCardCreateTime but no topCardInfo: " + uid);
        }
    }

    public WeiboCardView updateAndGetTopBlog(String uid, WeiboViewFormat format) {
        WeiboUserInfoCache userInfoCacahe = userInfoCacheRepository.findById(uid);
        if (userInfoCacahe == null) {
            log.warning("updateAndGetTopBlog but no userInfoCacahe: " + uid);
            return null;
        }
        TopCardInfo topCardInfo = topCardInfoRepository.findById(uid);
        if (topCardInfo == null) {
            topCardInfo = new TopCardInfo();
            topCardInfo.setUid(uid);
            log.info("topCardInfo create: " + uid);
        }


        try {
            JsonNode responseJson = weiboApiFeignClient.get(uid, API_TYPE_PARAM, uid, userInfoCacahe.getWeiboContainerid());
            JsonNode cardsNode = responseJson.get("data").get("cards");

            for (final JsonNode cardNode : cardsNode) {
                try {
                    JsonNode mblog = cardNode.get("mblog");

                    String mblog_created_at = mblog.get("created_at").asText();
                    ZonedDateTime utcZoned = ZonedDateTime.parse(mblog_created_at, dateTimeFormatter);
                    LocalDateTime localDateTime = utcZoned.toLocalDateTime();



                    String itemid = cardNode.get("itemid").asText();
                    String mblog_id = mblog.get("id").asText();
                    String mblog_text = mblog.get("text").asText();

                    List<String> picsLargeUrls = new ArrayList<>();
                    JsonNode picsNode = mblog.get("pics");
                    if (picsNode != null && picsNode.isArray()) {
                        for (final JsonNode picNode : picsNode) {
                            String largUrl = picNode.get("large").get("url").asText();
                            picsLargeUrls.add(largUrl);
                        }
                    }
                    boolean hasRetweet = mblog.has("retweeted_status");

                    WeiboCardCache cardCache;
                    cardCache = new WeiboCardCache();
                    cardCache.setItemid(itemid);
                    cardCache.setUid(uid);
                    cardCache.setBlogCreatedDateTime(localDateTime);
                    cardCache.setBlogText(mblog_text);
                    cardCache.setBlogId(mblog_id);
                    cardCache.setScreenName(userInfoCacahe.getScreenName());
                    cardCache.setPicsLargeUrls(picsLargeUrls);
                    cardCache.setRetweeted(hasRetweet);

                    //WeiboCardCacheAndImage cardCacheAndImage = handleImageFormat(cardCache, cacheFolder, format);

                    boolean isNew = topCardInfo.getCardCache() == null || topCardInfo.getCardCache().getBlogCreatedDateTime().isBefore(cardCache.getBlogCreatedDateTime()) ;
                    if (isNew) {
                        updateBlogDetail(cardCache);
                        topCardInfo.setCardCache(cardCache);
                        log.info("topCardInfo of " + uid + " updateTo card with CreatedDateTime = " + cardCache.getBlogCreatedDateTime().toString());
                    }

                } catch (Exception e) {
                    log.error("itera card error: " + cardNode, e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error("updateBlog: ", e);
            log.error("updateBlog error uid = " + uid);
        }
        WeiboCardView cardCacheAndImage = handleImageFormat(topCardInfo.getCardCache(), format);
        topCardInfoRepository.save(topCardInfo);
        return cardCacheAndImage;
    }



    private WeiboCardView handleImageFormat(WeiboCardCache cardCache, WeiboViewFormat format) {

        List<File> files = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        if (cardCache.getPicsLargeUrls() != null) {
            urls.addAll(cardCache.getPicsLargeUrls());
        }

        switch (format) {
            case FIRST_IMAGE:
                File imageFile = removeUrlToImage(urls, 0);
                if (imageFile != null) {
                    files.add(imageFile);
                }
                break;
            case ALL_IMAGE:
                files = removeUrlsToImages(urls);
                break;
            case NO_IMAGE:
            default:
                files = new ArrayList<>(0);
        }


        return new WeiboCardView(cardCache, files, urls);
    }

    private List<File> removeUrlsToImages(List<String> urls) {
        List<File> files = new ArrayList<>(urls.size());

        while (!urls.isEmpty()) {
            File file = removeUrlToImage(urls, 0);
            files.add(file);
        }

        return files;
    }

    private File removeUrlToImage(List<String> urls, int index) {
        if (urls.size() > index) {
            int lastSlash = urls.get(index).lastIndexOf("/");
            String id = urls.get(index).substring(lastSlash + 1);
            File file = cacheableFileHelper.fromCacheOrProvider(id, uncachedWeiboImageProvider);
            urls.remove(index);
            return file;
        } else {
            return null;
        }
    }



    
    public InputStream downloadUncachedWeiboImage(String fileId) {
        try {
            final Response response = weiboPictureApiFeignClient.pictures(fileId);
            final Response.Body body = response.body();
            final InputStream inputStream = body.asInputStream();
            return inputStream;
        } catch (Exception e) {
            log.error("download image faild by " + fileId, e);
            return null;
        }
    }


}
