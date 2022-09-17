package hundun.miraifleet.framework;

/**
 * @author hundun
 * Created on 2021/12/18
 */
public class DataTest {

//    public static class MyData extends JAutoSavePluginData {
//        public static final MyData INSTANCE = new MyData();
//        public MyData() { super("testSaveName"); }
//        public final Value<String> strMember = value("str");
//        public final Value<Integer> intMember = value(1);
//        public final Value<List<Long>> list = typedValue(
//                createKType(List.class, createKType(Long.class)),
//                new ArrayList<>()
//                ); // 无默认值, 自动创建空 List
//    }
//    
//    public static class MockPlugin extends JavaPlugin {
//        public static final MockPlugin INSTANCE = new MockPlugin();
//        public MockPlugin() {
//            super(new JvmPluginDescriptionBuilder("org.test.test", "1.0.0").build());
//        }
//    }
//
//    MiraiConsoleImplementationTerminal consoleInstance;
//
//    @Before
//    public void beforeTest() {
//        consoleInstance = new MiraiConsoleImplementationTerminal();
//        MiraiConsoleTerminalLoader.INSTANCE.startAsDaemon(consoleInstance);
//        PluginManager.INSTANCE.loadPlugin(MockPlugin.INSTANCE);
//        PluginManager.INSTANCE.enablePlugin(MockPlugin.INSTANCE);
//    }
//
//    @After
//    public void afterTest() {
//        // something likes MiraiConsole.cancel in java e.g.:
//        // consoleInstance.cancel
//    }
//
//    @Test
//    public void testData() {
//        MockPlugin.INSTANCE.reloadPluginData(MyData.INSTANCE);
//        assertEquals("str", MyData.INSTANCE.strMember.get());
//        assertEquals(Integer.valueOf(1), MyData.INSTANCE.intMember.get());
//    }

}
