package hundun.miraifleet.framework.core.helper.repository;

import java.io.File;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/12
 */
public class SingletonDocumentRepository<V> extends FileRepository<V> {
 
    private static final String THE_SINGLETON_KEY = "SINGLETON";
    
    public SingletonDocumentRepository(
            JvmPlugin plugin, 
            File file, 
            Class<V> documentClazz
            ) {
        super(plugin, file, documentClazz);
    }
    

    public V findSingleton() {
        readLock.lock();
        try {
            return data.contains(THE_SINGLETON_KEY) ? null : data.get(THE_SINGLETON_KEY);
        } finally {
            readLock.unlock();
        }
    }
    
    public void saveSingleton(V item) {
        writeLock.lock();
        try {
            data.put(THE_SINGLETON_KEY, item);
            writeFile();
        } finally {
            writeLock.unlock();
        }
    }

}
