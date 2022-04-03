package hundun.miraifleet.framework;

import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.console.MiraiConsoleImplementation;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JAutoSavePluginData;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author hundun
 * Created on 2021/12/18
 */
public abstract class AbstractJConsoleTest {

    MiraiConsoleImplementation consoleInstance;

    @Before
    public void initializeConsole() {
//        consoleInstance = new MiraiConsoleImplementationTerminal();
//        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon((MiraiConsoleImplementationTerminal) consoleInstance);
    }

    @After
    public void cancelConsole() {
        // need something likes MiraiConsole.cancel in java :
        // consoleInstance.cancel() or MiraiConsoleTerminalLoader.INSTANCE.cancelConsole()
    }

}
