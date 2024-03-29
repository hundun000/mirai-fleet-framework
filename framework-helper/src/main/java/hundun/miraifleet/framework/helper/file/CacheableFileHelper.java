package hundun.miraifleet.framework.helper.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.function.Function;

import hundun.miraifleet.framework.helper.Utils;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * @author hundun
 * Created on 2021/05/06
 */
public class CacheableFileHelper {
    
    File rootCacheFolder;
    MiraiLogger log;
    
    static final String DEFAULT_SUBFOLER_NAME = "default";
    String subFolerName;
    
    public CacheableFileHelper(File rootCacheFolder, MiraiLogger log) {
        this(rootCacheFolder, DEFAULT_SUBFOLER_NAME, log);
    }

    public CacheableFileHelper(File rootCacheFolder, String subFolerName, MiraiLogger log) {
        this.rootCacheFolder = rootCacheFolder;
        this.subFolerName = subFolerName;
        this.log = log;
    }

    private File fileIdToFile(String fileId) {
        String subFolerPathName = Utils.checkFolder(subFolerName, rootCacheFolder.getAbsolutePath());
        String saveFilePathName = subFolerPathName + File.separator + fileId;
        File file = new File(saveFilePathName);

        return file;
    }

    /**
     * 命中缓存：从缓存中读出file，返回之<br>
     * 未命中缓存：使用Provider得到InputStream，存为缓存file，返回之
     */
    public File fromCacheOrProvider(String fileId, Function<String, InputStream> uncachedFileProvider) {
        File file = fileIdToFile(fileId);
        if (file.exists()) {
            log.debug("file from cache for id: " + fileId);
        } else {
            InputStream inputStream = uncachedFileProvider.apply(fileId);

            if (inputStream == null) {
                log.info("provider return null for id: " + fileId);
                return null;
            }

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=inputStream.read(buf)))
                {
                   out.write(buf, 0, n);
                }
                out.close();
                inputStream.close();
                byte[] outBytes = out.toByteArray();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(outBytes);
                fos.close();
            } catch (Exception e) {
                log.error("FileOutputStream faild", e);
            }

            if (file != null && file.exists()) {
                log.info("file from provider and success: " + fileId);
            } else {
                log.warning("file from provider but fail: " + fileId);
            }
        }
        return file;
    }






}
