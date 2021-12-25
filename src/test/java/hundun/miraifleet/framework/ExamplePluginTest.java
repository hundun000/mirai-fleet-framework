package hundun.miraifleet.framework;


import org.junit.After;
import org.junit.Before;
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
    static MiraiConsoleImplementationTerminal consoleInstance;

    public static void main(String[] args) {
        consoleInstance = new MiraiConsoleImplementationTerminal();
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(consoleInstance);
        PluginManager.INSTANCE.loadPlugin(ExamplePlugin.INSTANCE);
        PluginManager.INSTANCE.enablePlugin(ExamplePlugin.INSTANCE);
    }
}
