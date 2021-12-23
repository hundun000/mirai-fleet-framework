package hundun.miraifleet.framework;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JAutoSavePluginData;
import net.mamoe.mirai.console.plugin.PluginManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal;
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader;

/**
 * @author hundun
 * Created on 2021/12/18
 */
public class DataTest {

    
/*    public static class MyData extends JAutoSavePluginData {
        public static final MyData INSTANCE = new MyData();
        public MyData() { super("testSaveName"); }
        public final Value<String> strMember = value("str");
        public final Value<Integer> intMember = value(1);
    }
    
    public static class MockPlugin extends JavaPlugin {
        public static final MockPlugin INSTANCE = new MockPlugin();
        public MockPlugin() {
            super(new JvmPluginDescriptionBuilder("org.test.test", "1.0.0").build());
        }
    }

    @Before
    public void beforeTest() {
        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(new MiraiConsoleImplementationTerminal());
        PluginManager.INSTANCE.loadPlugin(MockPlugin.INSTANCE);
    }
    
    @Test
    public void testData() {
        MockPlugin.INSTANCE.reloadPluginData(MyData.INSTANCE);
        assertEquals("str", MyData.INSTANCE.strMember.get());
        assertEquals(Integer.valueOf(1), MyData.INSTANCE.intMember.get());
    }*/

}
