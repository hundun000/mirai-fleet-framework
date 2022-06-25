### 1. 环境准备

- java 11
- [mirai-console](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
- 确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)  

### 2. 下载本项目制品

- mirai-fleet-XXX-XXX.mirai.jar

插件本体，放入mirai-console的plugins文件夹。

- ConfigAndData.zip

解压后得到config、data合并至mirai-console的同名文件夹。若解压后没有config文件夹，说明该版本已不需要人工准备文件夹，插件会自动使用样例值创建。

### 3. 配置mirai-console权限

本插件的指令受mirai-console权限系统管理，[权限授予说明](https://github.com/mamoe/mirai/blob/dev/docs/ConsoleTerminal.md#%E5%9C%A8%E7%BE%A4%E8%81%8A%E4%B8%AD%E4%BD%BF%E7%94%A8%E5%91%BD%E4%BB%A4-%E6%9D%83%E9%99%90%E6%8E%88%E4%BA%88)。本文后续只描述“需要将某个权限授予给某个被许可人”，具体操作方法见上述官方文档。

使用本插件时，一方面和常规插件一样，需要授予不同用户对应的指令权限（`hundun.fleet.XXX:command.XXX`或`hundun.fleet.XXX:*`）。另一方面，还需要授予一种fleet框架插件的`特殊权限`。注意，是两种权限都要授予。

#### 特殊权限设计说明

常规的指令权限只能控制指令，为了控制那些指令以外的形式触发的功能（戳一戳事件响应，定时自动发送），引入`特殊权限`作为本插件所有服务开关，具体的`<特殊权限id>`见具体插件的说明文档(例如对于`阿米娅插件`，则`<特殊权限id>`为：`hundun.fleet.amiya.cos:INSTANCE`)。

- 特殊权限授予给“群成员身份的bot”时，表示特定群的特定bot，启用该插件（即包括事件响应，定时自动发送）。

> 阿米娅插件示例，表示群111111里的bot账号222222，启用阿米娅插件。
>
> /perm permit m111111.222222 hundun.fleet.amiya.cos:INSTANCE  

- 特殊权限授予给“用户身份的人”时，表示任意bot，对好友身份的特定人，启用该插件（即包括事件响应，对好友一般不含定时自动发送）。

> 阿米娅插件示例，表示任意bot对好友333333，启用阿米娅插件。
>
> /perm permit u333333 hundun.fleet.amiya.cos:INSTANCE  


#### 进阶用法例子

阿米娅插件为例。console里运行了botA(id=222222)和botB(id=333333)，加入了同一个群(id=111111)，只希望群里botA启用本插件，botB不启用本插件，则可：

一方面，授予群111111任意群员使用指令的权限。
> /perm permit m111111.\* hundun.fleet.amiya:\*  

另一方面，授予botA特殊权限，不授予botB特殊权限。
> /perm permit m111111.222222 hundun.fleet.amiya.cos:INSTANCE

### 4. 启动和登录

启动mirai-console，在mirai-console里登录。
