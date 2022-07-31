package hundun.miraifleet.framework.helper.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

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

    @Nullable
    final Supplier<Map<String, V>> defaultDataSupplier;

    @FunctionalInterface
    public interface IdSetter<V, K> {
        void apply(V item, K id);
    }

    public FileRepository(
            JvmPlugin plugin,
            File file,
            Class<V> documentClazz,
            @Nullable Supplier<Map<String, V>> defaultDataSupplier
            ) {
        this.file = file;
        this.data = new ConcurrentHashMap<>();
        this.plugin = plugin;
        this.documentClazz = documentClazz;
        this.defaultDataSupplier = defaultDataSupplier;
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // not work
        //javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        this.objectMapper = new ObjectMapper()
                .registerModule(javaTimeModule)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)
                ;
        readFile();
    }


    protected void writeFile() {
        try {
            if (!file.exists()) {
                plugin.getLogger().info("file of " + documentClazz.getSimpleName() + "not exists, write will create empty.");
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            byte[] strToBytes = objectMapper.writeValueAsBytes(data);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            plugin.getLogger().error(e);
        }
    }


    protected void readFile() {
        try {
            if (!file.exists()) {
                plugin.getLogger().info("file of " + documentClazz.getSimpleName() + " not exists, read will create empty or default.");
                data.clear();
                if (defaultDataSupplier != null) {
                    data.putAll(defaultDataSupplier.get());
                }
                writeFile();
            }
            data = objectMapper.readValue(file, objectMapper.getTypeFactory().constructMapType(ConcurrentHashMap.class, String.class, documentClazz));
        } catch (IOException e) {
            plugin.getLogger().error(e);
        }
    }
}
