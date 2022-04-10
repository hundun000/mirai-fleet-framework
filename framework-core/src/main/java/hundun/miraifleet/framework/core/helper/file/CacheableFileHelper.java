package hundun.miraifleet.framework.core.helper.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.function.Function;

import hundun.miraifleet.framework.core.helper.Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hundun
 * Created on 2021/05/06
 */
@Slf4j
public class CacheableFileHelper {
    
    File rootCacheFolder;
    
    public CacheableFileHelper(File rootCacheFolder) {
        this.rootCacheFolder = rootCacheFolder;
    }


    private File cacheIdToFile(String fileId) {
        String subFolerName = "FileCache";
        String subFolerPathName = Utils.checkFolder(subFolerName, rootCacheFolder.getAbsolutePath());
        String saveFilePathName = subFolerPathName + File.separator + fileId;
        File file = new File(saveFilePathName);

        return file;
    }

    public File fromCacheOrProvider(String fileId, Function<String, InputStream> uncachedFileProvider) {
        //String subFolerName = "FileCache";
        File file = cacheIdToFile(fileId);
        if (file.exists()) {
            log.debug("image from cache :{}", fileId);
        } else {
            InputStream inputStream = uncachedFileProvider.apply(fileId);

            if (inputStream == null) {
                log.info("provider not support download, image null for: {}", fileId);
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
                log.info("FileOutputStream success: {}", fileId);
            } catch (Exception e) {
                log.info("FileOutputStream faild {} {}", fileId, e);
                return null;
            }

            if (file != null && file.exists()) {
                log.info("image from download and success :{}", fileId);
            } else {
                log.warn("image from download but fail :{}", fileId);
            }
        }
        return file;
    }






}
