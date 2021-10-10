package hundun.miraifleet.framework.core.helper.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2021/08/12
 */
public class MapDocumentRepository<V> extends FileRepository<V> {
    

    final Function<V, String> idGetter;
    final IdSetter<V, String> idSetter;
    //final Function<String, V> createFunction;
    
    @FunctionalInterface
    public interface IdSetter<V, K> {
        void apply(V item, K id);
    }
    
    public MapDocumentRepository(
            JvmPlugin plugin, 
            File file, 
            Class<V> documentClazz,
            Function<V, String> idGetter,
            IdSetter<V, String> idSetter
            ) {
        super(plugin, file, documentClazz);
        this.idGetter = idGetter;
        this.idSetter = idSetter;

    }

    
    public void deleteAll() {
        writeLock.lock();
        try {
            data.clear();
            writeFile();
        } finally {
            writeLock.unlock();
        }
    }
    
    
    

    public void delete(V item) {
        writeLock.lock();
        try {
            String id = idGetter.apply(item);
            if (id != null) {
                deleteById(id);
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    public void deleteById(String id) {
        writeLock.lock();
        try {
            data.remove(id);
            writeFile();
        } finally {
            writeLock.unlock();
        }
    }

    public void save(V item) {
        writeLock.lock();
        try {
            saveAndWriteFile(item, true);
        } finally {
            writeLock.unlock();
        }
    }
    
    protected void saveAndWriteFile(V item, boolean writeFile) {
        String id = idGetter.apply(item);
        if (id == null) {
            id = "AUTO_" + UUID.randomUUID().toString();
            idSetter.apply(item, id);
        }
        data.put(id, item);
        if (writeFile) {
            writeFile();
        }
        
    }

    
    public void saveAll(List<V> items) {
        writeLock.lock();
        try {
            for (V item: items) {
                saveAndWriteFile(item, false);
            }
            writeFile();
        } finally {
            writeLock.unlock();
        }
    }

    
    
    public V findById(String id) {
        readLock.lock();
        try {
            return data.get(id);
        } finally {
            readLock.unlock();
        }
    }
    
    public List<V> findAll() {
        readLock.lock();
        try {
            return findAllByFilter(null, null, null);
        } finally {
            readLock.unlock();
        }
    }

    public <T_FIELD> List<V> findAllByFilter(Integer topLimit, Function<V, T_FIELD> filterFieldGetter, T_FIELD filterValue) {
        readLock.lock();
        try {
            List<V> result = new ArrayList<>();
            for (Entry<String, V> entry : data.entrySet()) {
                boolean notReachLimit = topLimit == null || result.size() < topLimit;
                if (!notReachLimit) {
                    break;
                }
                boolean notFilter = filterFieldGetter == null || filterValue == null;
                boolean filterMatched = notFilter || Objects.equals(filterFieldGetter.apply(entry.getValue()), filterValue);
                if (filterMatched) {
                    result.add(entry.getValue());
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }
    
    public <T_FIELD> V findOneByFilter(Function<V, T_FIELD> filterFieldGetter, T_FIELD filterValue) {
        readLock.lock();
        try {
            List<V> result = findAllByFilter(1, filterFieldGetter, filterValue);
            return result.isEmpty() ? null : result.get(0);
        } finally {
            readLock.unlock();
        }    
    }
    
    public boolean existsById(String id) {
        readLock.lock();
        try {
            return data.containsKey(id);
        } finally {
            readLock.unlock();
        }
    }
}
