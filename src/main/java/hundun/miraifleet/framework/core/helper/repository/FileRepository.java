package hundun.miraifleet.framework.core.helper.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * 
 * @author hundun
 * Created on 2021/08/12
 */
public abstract class FileRepository<V> {
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwlock.readLock();
    protected final Lock writeLock = rwlock.writeLock();
    private final ObjectMapper objectMapper;
    
    private File file;
    protected final JvmPlugin plugin;
    
    protected ConcurrentHashMap<String, V> data;
    private final Class<V> documentClazz;

    
    @FunctionalInterface
    public interface IdSetter<V, K> {
        void apply(V item, K id);
    }
    
    public FileRepository(
            JvmPlugin plugin, 
            File file, 
            Class<V> documentClazz
            ) {
        this.file = file;
        this.data = new ConcurrentHashMap<>();
        this.plugin = plugin;
        this.documentClazz = documentClazz;
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // not work
        //javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        this.objectMapper = new ObjectMapper()
                .registerModule(javaTimeModule)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                ;
        readDataList();
    }
    
    protected void writeFile() {
        try {
            if (!file.exists()) {
                plugin.getLogger().info("file of " + documentClazz.getSimpleName() + "not exists, will create empty.");
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            String jsonString = objectMapper.writeValueAsString(data);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] strToBytes = jsonString.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            plugin.getLogger().error(e);
        }
    }
    

    protected void readDataList() {
        try {
            if (!file.exists()) {
                plugin.getLogger().info("file of " + documentClazz.getSimpleName() + " not exists, will create empty.");
                writeFile();
            }
            data = objectMapper.readValue(file, objectMapper.getTypeFactory().constructMapType(ConcurrentHashMap.class, String.class, documentClazz));
        } catch (IOException e) {
            plugin.getLogger().error(e);
        }
    }
}
