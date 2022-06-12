package hundun.miraifleet.framework.helper.repository;

import java.io.File;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/12
 */
public class SingletonDocumentRepository<V> extends FileRepository<V> {

    public static final String THE_SINGLETON_KEY = "SINGLETON";

    public SingletonDocumentRepository(
            JvmPlugin plugin,
            File file,
            Class<V> documentClazz
            ) {
        this(plugin, file, documentClazz, null);
    }

    public SingletonDocumentRepository(
            JvmPlugin plugin,
            File file,
            Class<V> documentClazz,
            @Nullable Supplier<V> defaultDataSupplier
            ) {
        super(plugin, file, documentClazz, 
                // singleton-Supplier to map-Supplier
                () -> Map.of(THE_SINGLETON_KEY, defaultDataSupplier.get())
                );
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

    public void deleteSingleton() {
        writeLock.lock();
        try {
            data.remove(THE_SINGLETON_KEY);
            writeFile();
        } finally {
            writeLock.unlock();
        }
    }

}
