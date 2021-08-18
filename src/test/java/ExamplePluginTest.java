

import hundun.miraifleet.framework.example.AmiyaExamplePlugin;
import hundun.miraifleet.framework.example.PrinzEugenExamplePlugin;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
/**
 * @author hundun
 * Created on 2021/06/03
 */
public class ExamplePluginTest {
    public static void main(String[] args) throws InterruptedException {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
        
        PluginManager.INSTANCE.loadPlugin(AmiyaExamplePlugin.INSTANCE);
        
        PluginManager.INSTANCE.enablePlugin(AmiyaExamplePlugin.INSTANCE);
        
        PluginManager.INSTANCE.loadPlugin(PrinzEugenExamplePlugin.INSTANCE);
        
        PluginManager.INSTANCE.enablePlugin(PrinzEugenExamplePlugin.INSTANCE);
    }
}
