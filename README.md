[![](https://jitpack.io/v/hundun000/mirai-fleet-framework.svg)](https://jitpack.io/#hundun000/mirai-fleet-framework)

来自[ZacaFleetBot]()项目的拆分方案。故框架的大部分来自原项目，适合熟悉该框架的开发者 ~~（好像只有我）~~ ，不一定适合刚接触mirai插件开发的人。

- 原本数个Character共享ZacaFleetBot的core包，拆分后是数个插件项目依赖本项目的core包。
- 本项目starter包提供少量常用的Function类。其他插件项目使用core包创建自己特有的Function类。
- 本项目的example是一个使用core包的插件项目样例，其可打包出一个Plugin。
- 试着采用社区建议，不再依赖Spring和MongoDB。

使用本框架的插件项目：

- [欧根插件(砍口垒助手)]()
- [阿米娅插件(明日方舟助手)]()


### 开发说明


### BotLogic

Plugin持有一个对应的BotLogic。保持Plugin只处理和mirai对接工作，详细的业务逻辑由BotLogic负责。

详细的业务逻辑包括：

- 注册Command和ListenHost。对于使用本框架的开发者，实际是向BotLogic注册Function，然后框架会再把Function作为Command和ListenHost注册到mirai。

- 一些供Function共用的方法。

### 配置化目标

希望支持如下情况：在一个console里放入AmiyaPlugin和PrinzEugenPlugin，登录两个bot，两个bot均加在同一个群111111。配置后，在这同一个群里，AmiyaBot只回应AmiyaPlugin的Command/EventHandler/Timer，且使用AmiyaPlugin的配置；PrinzEugenBot同理。





### Function

对所有Function，

1. 业务方法入口执行检查：pluginInstance.check(botId, functionName, contactId)

> amiyaPluginInstance.check(amiyaBotId, weibo, arknightGroup) == true
> amiyaPluginInstance.check(amiyaBotId, weibo, kancolleGroup) == false 
> amiyaPluginInstance.check(amiyaBotId, weibo, testGroup) == true

> prinzEugenPluginInstance.check(prinzEugenBotId, weibo, arknightGroup) == false
> prinzEugenPluginInstance.check(prinzEugenBotId, weibo, kancolleGroup) == true
> prinzEugenPluginInstance.check(prinzEugenBotId, weibo, testGroup) == true

2. 业务逻辑内的上下文不关心请求的botId，最多关心contactId：sessionId = pluginInstance.function.sessionDataMap.get(contactId)

> sessionData = amiyaPluginInstance.repeatFunction.sessionDataMap.get(arknightGroupId);
> SessionData.lastMessage == theLastMessageInArknightGroup;
> // kancolleGroup's request will not into amiyaPluginInstance.repeatFunction's main-business-code-area
> sessionData = amiyaPluginInstance.repeatFunction.sessionDataMap.get(testGroupId);
> SessionData.lastMessage == theLastMessageInTestGroup;

> // arknightGroup's request will not into prinzEugenPluginInstance.repeatFunction's main-business-code-area
> sessionData = prinzEugenPluginInstance.repeatFunction.sessionDataMap.get(kancolleGroupId);
> sessionData.lastMessage == theLastMessageInKancolleGroup;
> sessionData = prinzEugenPluginInstance.repeatFunction.sessionDataMap.get(testGroupId);
> sessionData.lastMessage == theLastMessageInTestGroup;


3. 业务逻辑内的配置不关心请求的botId和contactId

> weiboConfig = amiyaPluginInstance.weiboFunction.configRepository.getSingleton();
> weiboConfig.listen == [明日方舟, 朝陇山, 泰拉漫画]
> // amiyaPluginInstance.weiboFunction.timer.sendNewWeibo() iterate all groups, but filtered in sendNewWeibo()'s entrance

> weiboConfig = prinzEugenPluginInstance.weiboFunction.configRepository.getSingleton();
> weiboConfig.listen == [舰C镇守府情报]
> weiboConfig = prinzEugenPluginInstance.weiboFunction.configRepository.getSingleton();
> // prinzEugenPluginInstance.weiboFunction.timer.sendNewWeibo() iterate all groups, but filtered in sendNewWeibo()'s entrance

4. 加上权限管理
```
// only adminUser has console permission
/permission cancelall console * 
/permission permit console u[adminUserId]

// only testGroup and kancolleGroup has prinzeugenPlugin permission
/permission cancelall hundun.fleet.example.prinzeugen * 
/permission cancelall hundun.fleet.example.prinzeugen m[testGroupId].*
/permission cancelall hundun.fleet.example.prinzeugen m[kancolleGroupId].*
```
