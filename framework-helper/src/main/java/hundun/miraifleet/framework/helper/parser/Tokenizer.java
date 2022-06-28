package hundun.miraifleet.framework.helper.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;


/**
 * @author hundun
 * Created on 2021/04/27
 */
public class Tokenizer<T> {
    

    private String KEYWORD_WAKE_UP = "UNSETTED";
    
    private Map<String, TokenType> keywords = new HashMap<>();
    private Map<String, T> functionIdentifiers = new HashMap<>();
    
    
    public Tokenizer() {
    }


    public List<Token<T>> simpleTokenize(Message message) {
        List<Token<T>> result = new ArrayList<>();
        if (message instanceof At) {
            At atMessage = (At)message;
            Token<T> token = new Token<>();
            token.setType(TokenType.AT);
            token.setTextContent(String.valueOf(atMessage.getTarget()));
            result.add(token);
        } else if (message instanceof PlainText) {
            PlainText plainTextMessage = (PlainText)message;
            String text = plainTextMessage != null ? plainTextMessage.contentToString() : null;
            if (text != null && text.trim().length() > 0) {
                List<String> subTexts = new ArrayList<>(Arrays.asList(text.split(" ")));
                subTexts = subTexts.stream().map(it -> it.trim()).filter(it -> !it.isEmpty()).collect(Collectors.toList());
                // special rule: split WAKE_UP from start
                if (subTexts.get(0).startsWith(KEYWORD_WAKE_UP) && subTexts.get(0).length() > KEYWORD_WAKE_UP.length()) {
                    String autoSplit = subTexts.get(0).substring(KEYWORD_WAKE_UP.length());
                    subTexts.set(0, KEYWORD_WAKE_UP);
                    subTexts.add(1, autoSplit);
                }
                
                for (String subText : subTexts) {
                    if (keywords.containsKey(subText)) {
                        Token<T> token = new Token<>();
                        token.setType(keywords.get(subText));
                        token.setTextContent(subText);
                        result.add(token);
                    } else if (functionIdentifiers.containsKey(subText)) {
                        T subFunction = functionIdentifiers.get(subText);
                        
                        Token<T> token = new Token<>();
                        token.setType(TokenType.FUNCTION_NAME);
                        token.setTextContent(subText);
                        token.setExtraContent(subFunction);
                        result.add(token);
                    } else {
                        Token<T> token = new Token<>();
                        token.setType(TokenType.LITERAL_VALUE);
                        token.setTextContent(subText);
                        result.add(token);
                    }
                }
            }
        }
        return result;
    }

    
    public void registerKeyword(String keyword, TokenType tokenType) throws Exception {
        if (keywords.containsKey(keyword)) {
            throw new Exception("已存在keyword = " + keywords.get(keyword));
        }
        this.keywords.put(keyword, tokenType);
        if (tokenType == TokenType.WAKE_UP) {
            this.KEYWORD_WAKE_UP = keyword;
        }
    }

    public void registerSubFunction(T subFunction, String customIdentifier) {
        functionIdentifiers.put(customIdentifier, subFunction);
    }


}
