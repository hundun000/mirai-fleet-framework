package hundun.miraifleet.framework.helper.parser;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/04/27
 */
@Data
public class Token<T> {
    private TokenType type;
    private String textContent;
    private T extraContent;
}
