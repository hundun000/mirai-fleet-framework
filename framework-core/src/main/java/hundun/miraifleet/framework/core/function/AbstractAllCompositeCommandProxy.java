package hundun.miraifleet.framework.core.function;

import hundun.miraifleet.framework.core.botlogic.BaseBotLogic;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * @author hundun
 * Created on 2022/02/10
 */
public abstract class AbstractAllCompositeCommandProxy<T extends BaseBotLogic> extends CompositeCommand {
    
    protected final T botLogic;
    
    public AbstractAllCompositeCommandProxy(
            T botLogic,
            JvmPlugin plugin,
            String characterName
            ) {
        super(plugin, 
                botLogic.getAllCompositeCommandProxyConfig().getPrimaryName(), 
                new String[]{}, 
                "我是" + characterName + "的聚合指令", 
                botLogic.getUserCommandRootPermission(), 
                CommandArgumentContext.EMPTY);
        this.botLogic = botLogic;
    }
    
    public static class CreateHelper {
        
    }
}
