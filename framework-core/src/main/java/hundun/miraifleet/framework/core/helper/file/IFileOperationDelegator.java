package hundun.miraifleet.framework.core.helper.file;

import java.io.File;
import java.io.InputStream;

/**
 * @Deprecated use CachableFileHelper
 * @author hundun
 * Created on 2021/05/06
 */
@Deprecated
public interface IFileOperationDelegator {

    InputStream downloadOrFromLocal(String fileId, File localDataFolder);

    File fromCacheOrDownloadOrFromLocal(String fileId, File rootCacheFolder, File localDataFolder);
}
