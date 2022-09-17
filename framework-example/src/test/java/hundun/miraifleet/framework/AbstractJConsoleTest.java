package hundun.miraifleet.framework;

import net.mamoe.mirai.console.MiraiConsoleImplementation;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;
import org.junit.After;
import org.junit.Before;
import kotlinx.coroutines.CoroutineScopeKt;

/**
 * @author hundun
 * Created on 2021/12/18
 */
public abstract class AbstractJConsoleTest {

    MiraiConsoleImplementation consoleInstance;

    @Before
    public void initializeConsole() {
        consoleInstance = new MiraiConsoleImplementationTerminal();
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon((MiraiConsoleImplementationTerminal) consoleInstance);
    }

    @After
    public void cancelConsole() {
        CoroutineScopeKt.cancel(consoleInstance, null);
    }

}
