package hundun.miraifleet.framework;


import org.junit.Test;

import hundun.miraifleet.framework.example.ExamplePlugin;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
/**
 * @author hundun
 * Created on 2021/06/03
 */
public class ExamplePluginTest {
 
    @Test
    public void test() {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
        
        PluginManager.INSTANCE.loadPlugin(ExamplePlugin.INSTANCE);
        
        PluginManager.INSTANCE.enablePlugin(ExamplePlugin.INSTANCE);
    }
}
