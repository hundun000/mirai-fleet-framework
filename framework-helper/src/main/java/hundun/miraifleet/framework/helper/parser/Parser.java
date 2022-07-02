package hundun.miraifleet.framework.helper.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import hundun.miraifleet.framework.helper.parser.statement.Statement;
import hundun.miraifleet.framework.helper.parser.statement.SyntaxsErrorStatement;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;



/**
 * @author hundun
 * Created on 2021/04/27
 */
@Slf4j
public abstract class Parser {

    private Tokenizer tokenizer = new Tokenizer();
    
    private SyntaxsTree syntaxsTree = new SyntaxsTree();
    
    private Map<StatementType, Function<List<Token>, Statement>> constructorFunctionMap = new HashMap<>();
    
    public Parser() {
        initParser();
    }
    
    protected abstract void initParser();
    
    public Statement simpleParse(MessageChain messageChain) {
        
        
        List<Token> tokens = new ArrayList<>();
        for (Message message : messageChain) {
            List<Token> newTokens = tokenizer.simpleTokenize(message);
            tokens.addAll(newTokens);
        }
        
        StatementType statementType = syntaxsTree.root.accept(tokens, 0);
        Statement statement;
        if (statementType == null || statementType == StatementType.SYNTAX_ERROR) {
            statementType = StatementType.SYNTAX_ERROR;
            statement = new SyntaxsErrorStatement();
        }  else {
            Function<List<Token>, Statement> function = constructorFunctionMap.get(statementType);
            statement = function.apply(tokens);
        }

        statement.setType(statementType);
        statement.setTokens(tokens);
        statement.setOriginMiraiCode(messageChain.serializeToMiraiCode());
        return statement;
    }
    
    public class SyntaxsTree {
        DFANode root = new DFANode();
        
        public void registerSyntaxs(List<List<TokenType>> grammars, StatementType type) {
            for (List<TokenType> grammar : grammars) {
                registerSyntax(grammar, type);
            }
        }
        
        public void registerSyntax(List<TokenType> grammar, StatementType type) {
            DFANode nowNode = root;

            for(int i = 0; i < grammar.size(); i++) {
                TokenType word = grammar.get(i);
                
                DFANode nextNode = nowNode.getChildNode(word);
                
                if (nextNode == null) {
                    nextNode = new DFANode();
                    nowNode.put(word, nextNode);
                }
                nowNode = nextNode;
                
                if (i == grammar.size() - 1) {
                    nowNode.endType = type;
                }
            }

            
        }
    }
    
    class DFANode {
        
        
        private Map<TokenType, DFANode> children;
        StatementType endType;
        
        public DFANode() {
            this.children = new HashMap<>();
            this.endType = null;
        }

        public DFANode getChildNode(TokenType input) {
            return children.get(input);
        }

        public void put(TokenType input, DFANode node) {
            if (children.containsKey(input)) {
                log.error("DFA node {} 已存在");
            }
            children.put(input, node);
        }
        
        /**
         * 特别地，当token的原type非accept时，会尝试把token类型改变为LITERAL_VALU再次检查
         */
        @Nullable
        public StatementType accept(List<Token> tokens, int currentIndex) {
            if (tokens == null) {
                return null;
            }
            if (tokens.size() > currentIndex) {
                Token top = tokens.get(currentIndex);
                DFANode nextNode = getChildNode(top.getType());
                if (nextNode == null) {
                    nextNode = getChildNode(TokenType.LITERAL_VALUE);
                    if (nextNode == null) {
                        return StatementType.SYNTAX_ERROR;
                    } else {
                        top.changeToLiteralValue();
                    }
                }
                return nextNode.accept(tokens, currentIndex + 1);
            } else {
                return endType;
            }
        }

        public int size() {
            return children.size();
        }
        
        public Set<TokenType> getKeySet(){ 
            Set<TokenType> set;
            set = children.keySet();
            return set;
        }
        
        @Override
        public String toString() {
            return children.toString();
        }
    }
    
    protected void registerSubCommand(String standardText, String... aliasTexts) {
        
        for (String aliasText : aliasTexts) {
            this.tokenizer.registerSubCommand(standardText, aliasText);
        }
    }
    
    
    



    
    
    protected void registerMainCommand(String keyword) {
        try {
            this.tokenizer.registerKeyword(keyword, TokenType.MAIN_COMMAND_NAME);
        } catch (Exception e) {
            log.error("registerWakeUpKeyword fail:{}", e.getMessage());
        }
    }
    
    protected void registerSyntaxs(
            Function<List<Token>, Statement> constructorFunction,
            List<List<TokenType>> syntaxs, 
            StatementType type) {
        this.syntaxsTree.registerSyntaxs(syntaxs, type);
        this.constructorFunctionMap.put(type, constructorFunction);
    }
    
}
