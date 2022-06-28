package hundun.miraifleet.framework.helper.parser;

import org.junit.Test;

import hundun.miraifleet.framework.helper.parser.statement.SubFunctionCallStatement;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

/**
 * @author hundun
 * Created on 2022/06/28
 */
public class ParserTest {
    
    static Parser<String> parser = new Parser<String>() {
        @Override
        protected void initParser() {
            
            
            registerWakeUpKeyword("pet");
            registerSubFunctionByCustomSetting("petpet", "摸");

            
            registerSyntaxs(SubFunctionCallStatement.syntaxs, StatementType.SUB_FUNCTION_CALL);

        }
    };
    
    
    

    @Test
    public void test() {
        MessageChain messageChain;
        
        messageChain = new PlainText("pet ").plus(new At(114514L)).plus(" 摸");
        System.out.println(parser.simpleParse(messageChain));
        
        messageChain = new PlainText("pet ").plus(new At(114514L)).plus(" 摸 text");
        System.out.println(parser.simpleParse(messageChain));
    }

}
