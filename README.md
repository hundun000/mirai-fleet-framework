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

希望支持如下情况：在一个console里放入AmiyaPlugin和PrinzEugenPlugin，登录两个bot，两个bot均加在同一个群111111。配置后，在这同一个群里，AmiyaBot222222只回应AmiyaPlugin的Command/EventHandler/Timer，且使用AmiyaPlugin的配置；PrinzEugenBot333333同理。

```
grantedPermissionMap: 
  'hundun.fleet.example.prinzeugen:*': 
    - 'm111111.*' #群111111可使用该插件指令
  'hundun.fleet.example.prinzeugen.cos:INSTANCE': 
    - m111111.333333 #Bot333333在群111111扮演该插件角色（欧根）
  'hundun.fleet.example.amiya:*': 
    - 'm111111.*' #群111111可使用该插件指令
  'hundun.fleet.example.amiya.cos:INSTANCE': 
    - m111111.222222 #Bot222222在群111111扮演该插件角色（阿米娅）
```


0. 以群11111发出一条AmiyaCommand为例。
1. 由于command权限配置，群11111的群员确实有AmiyaCommand的权限。
2. AmiyaCommand方法将会被调用两次，两次的CommandSender.getBot()分别是两个bot。
3. 对于bot==PrinzEugen，由于cos权限配置，其跳出了AmiyaCommand方法。
4. 对于bot==Amiya，由于cos权限配置，其可执行AmiyaCommand方法的后续代码。
5. AmiyaCommand方法里执行resolveConfig，一定是在AmiyaPlugin的目录下。


0. 以Console发出一条AmiyaCommand为例。
1. 由于command权限配置，console确实有AmiyaCommand的权限。
2. AmiyaCommand方法将会被调用1次，其中CommandSender instanceOf ConsoleCommandSender。
3. 对于ConsoleCommandSender，由于cos权限配置，其可执行AmiyaCommand方法的后续代码。
4. AmiyaCommand方法里执行resolveConfig，一定是在AmiyaPlugin的目录下。