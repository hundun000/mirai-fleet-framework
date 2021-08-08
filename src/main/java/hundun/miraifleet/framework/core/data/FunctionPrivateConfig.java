package hundun.miraifleet.framework.core.data;
/**
 * @author hundun
 * Created on 2021/08/10
 */

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class FunctionPrivateConfig {
    Set<Long> disabledContactIds = new HashSet<>(0);
}
