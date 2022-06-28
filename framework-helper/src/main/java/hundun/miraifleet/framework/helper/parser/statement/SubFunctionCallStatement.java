package hundun.miraifleet.framework.helper.parser.statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hundun.miraifleet.framework.helper.parser.Token;
import hundun.miraifleet.framework.helper.parser.TokenType;
import lombok.Getter;


/**
 * @author hundun
 * Created on 2021/04/27
 */
@Getter
public class SubFunctionCallStatement<T> extends Statement<T> {
    
    public static List<List<TokenType>> syntaxs = new ArrayList<>();
    static {
        syntaxs.add(Arrays.asList(TokenType.WAKE_UP, TokenType.AT, TokenType.FUNCTION_NAME));
        syntaxs.add(Arrays.asList(TokenType.WAKE_UP, TokenType.AT, TokenType.FUNCTION_NAME, TokenType.LITERAL_VALUE));
    }
    
    T subFunction;
    List<String> args;
    
    public SubFunctionCallStatement(List<Token<T>> tokens) {
        this.subFunction = tokens.get(1).getExtraContent();
        int argsStartIndex = 2;
        this.args = new ArrayList<>(tokens.size() - argsStartIndex);
        for (int i = argsStartIndex; i < tokens.size(); i ++) {
            this.args.add(tokens.get(i).getTextContent());
        }
    }
}
