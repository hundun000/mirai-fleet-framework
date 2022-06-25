package hundun.miraifleet.framework.helper.parser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.sisu.Nullable;
import org.junit.Test;

import hundun.miraifleet.framework.helper.parser.statement.Statement;
import hundun.miraifleet.framework.helper.parser.statement.SubFunctionCallStatement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

/**
 * @author hundun
 * Created on 2022/06/28
 */
public class ParserTest {
    
    @Getter
    private static class PetpetStatement extends Statement {
        
        public static List<List<TokenType>> syntaxs = new ArrayList<>();
        static {
            // petpet @114514
            syntaxs.add(Arrays.asList(
                    TokenType.SUB_COMMAND_NAME, 
                    TokenType.AT
                    ));
            // pet @114514 petpet
            syntaxs.add(Arrays.asList(
                    TokenType.MAIN_COMMAND_NAME, 
                    TokenType.AT, 
                    TokenType.SUB_COMMAND_NAME
                    ));
            // pet @114514 petpet fooText
            syntaxs.add(Arrays.asList(
                    TokenType.MAIN_COMMAND_NAME, 
                    TokenType.AT, 
                    TokenType.SUB_COMMAND_NAME, 
                    TokenType.LITERAL_VALUE
                    ));
            // pet @114514 petpet [image] fooText
            syntaxs.add(Arrays.asList(
                    TokenType.MAIN_COMMAND_NAME, 
                    TokenType.AT, 
                    TokenType.SUB_COMMAND_NAME, 
                    TokenType.IMAGE,
                    TokenType.LITERAL_VALUE
                    ));
        }
        
        /**
         * petept模板id
         */
        String templateId;
        @Nullable
        At at;
        @Nullable
        Image image;
        /**
         * 用于替换填充模板的文本
         */
        List<String> additionTexts = new ArrayList<>();
        
        public PetpetStatement(List<Token> tokens) {
            
            for (Token token : tokens) {
                switch (token.getType()) {
                    case SUB_COMMAND_NAME:
                        this.templateId = Objects.requireNonNull(token.getExtraTextContent());
                        break;
                    case LITERAL_VALUE:
                        this.additionTexts.add(Objects.requireNonNull(token.getTextContent()));
                        break;
                    case AT:
                        this.at = Objects.requireNonNull(token.getAtContent());
                        break;
                    case IMAGE:
                        this.image = Objects.requireNonNull(token.getImageContent());
                        break;
                    default:
                        break;
                }
            }

        }
    }
    
    
    static class PetpetParser extends Parser {
        @Override
        protected void initParser() {
            
            
            registerMainCommand("pet");
            registerSubCommand("petpet", "摸", "摸爆");

            
            registerSyntaxs(
                    tokens -> new PetpetStatement(tokens),
                    PetpetStatement.syntaxs, 
                    StatementType.COMMAND_CALL
                    );

        }
    };
    
    static PetpetParser parser = new PetpetParser();
    
    MessageChain messageChain;
    Statement statement;
    PetpetStatement petpetStatement;
    static final String offLineImageFakeId = "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg";
    Image actualImage = Image.fromId(offLineImageFakeId);
    long actualAtTarget = 114514L;
    
    @Test
    public void testImageCommand() {
        
        messageChain = new PlainText("pet ")
                .plus(new At(actualAtTarget))
                .plus(" 摸")
                .plus(actualImage)
                .plus(new PlainText("嘻嘻"))
                ;
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.COMMAND_CALL, statement.getType());
        
        petpetStatement = (PetpetStatement) statement; 
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(1, petpetStatement.getAdditionTexts().size());
        assertEquals("嘻嘻", petpetStatement.getAdditionTexts().get(0));
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());
        assertEquals(actualImage, petpetStatement.getImage());
        
    }
    
    @Test
    public void testNoMainName() {
        messageChain = new PlainText("摸 ").plus(new At(actualAtTarget));
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.COMMAND_CALL, statement.getType());
        // 输出
        petpetStatement = (PetpetStatement) statement; 
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(0, petpetStatement.getAdditionTexts().size());
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());
    }
    
    @Test
    public void testMainName() {
        
        // 输入
        messageChain = new PlainText("pet ").plus(new At(actualAtTarget)).plus(" 摸");
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.COMMAND_CALL, statement.getType());
        // 输出
        petpetStatement = (PetpetStatement) statement; 
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(0, petpetStatement.getAdditionTexts().size());
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());

        messageChain = new PlainText("pet ").plus(new At(114514L)).plus(" 摸 嘻嘻");
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.COMMAND_CALL, statement.getType());
        petpetStatement = (PetpetStatement) statement; 
        assertEquals("petpet", petpetStatement.getTemplateId());
        assertEquals(1, petpetStatement.getAdditionTexts().size());
        assertEquals("嘻嘻", petpetStatement.getAdditionTexts().get(0));
        assertEquals(actualAtTarget, petpetStatement.getAt().getTarget());
        
        
        
    }

    
    @Test
    public void testNotCommand() {
        messageChain = new PlainText("pet 嘻嘻").plus("");
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.SYNTAX_ERROR, statement.getType());
        assertEquals("pet 嘻嘻", statement.getOriginMiraiCode());
        
        messageChain = new PlainText("嘻嘻").plus("");
        statement = parser.simpleParse(messageChain);
        assertEquals(StatementType.SYNTAX_ERROR, statement.getType());
        assertEquals("嘻嘻", statement.getOriginMiraiCode());
    }
}
