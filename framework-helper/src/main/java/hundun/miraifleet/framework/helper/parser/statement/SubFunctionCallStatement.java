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
@Deprecated
@Getter
public class SubFunctionCallStatement extends Statement {
    
    public static List<List<TokenType>> syntaxs = new ArrayList<>();
    static {
        syntaxs.add(Arrays.asList(TokenType.MAIN_COMMAND_NAME, TokenType.AT, TokenType.SUB_COMMAND_NAME));
        syntaxs.add(Arrays.asList(TokenType.MAIN_COMMAND_NAME, TokenType.AT, TokenType.SUB_COMMAND_NAME, TokenType.LITERAL_VALUE));
    }
    
    String subFunction;
    List<String> args;
    
    public SubFunctionCallStatement(List<Token> tokens) {
        this.subFunction = tokens.get(1).getExtraTextContent();
        int argsStartIndex = 2;
        this.args = new ArrayList<>(tokens.size() - argsStartIndex);
        for (int i = argsStartIndex; i < tokens.size(); i ++) {
            this.args.add(tokens.get(i).getTextContent());
        }
    }
}
