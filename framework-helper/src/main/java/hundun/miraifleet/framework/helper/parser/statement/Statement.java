package hundun.miraifleet.framework.helper.parser.statement;

import java.util.List;

import hundun.miraifleet.framework.helper.parser.StatementType;
import hundun.miraifleet.framework.helper.parser.Token;
import lombok.Data;

/**
 * @author hundun
 * Created on 2021/04/27
 * @param <T>
 */
@Data
public abstract class Statement<T> {
    List<Token<T>> tokens;
    StatementType type;
    String originMiraiCode;
}
