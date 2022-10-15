package hundun.miraifleet.framework.starter.botlogic.function.character;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hundun
 * Created on 2022/10/25
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CharacterAdminHelperFunctionConfig {
    Set<Long> permittedGroupInviters;
}
