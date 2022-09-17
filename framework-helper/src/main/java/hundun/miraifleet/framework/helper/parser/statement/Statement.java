package hundun.miraifleet.framework.helper.parser.statement;

import java.util.List;
import hundun.miraifleet.framework.helper.parser.StatementType;
import hundun.miraifleet.framework.helper.parser.Token;
import lombok.Data;

/**
 * @author hundun
 * Created on 2021/04/27
 * @param 
 */
@Data
public abstract class Statement {
    protected List<Token> tokens;
    protected StatementType type;
    protected String originMiraiCode;
}
