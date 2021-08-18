package hundun.miraifleet.framework.core.helper.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/12
 */
public class PluginConfigRepository<V> extends FileRepository<V> {
 
    private static final String THE_SINGLETON_KEY = "SINGLETON";
    
    public PluginConfigRepository(
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
