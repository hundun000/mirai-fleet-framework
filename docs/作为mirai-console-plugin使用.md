### 1. 环境准备

- java 11
- [mirai-console](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
- 确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)  

### 2. 下载本项目制品

- mirai-fleet-XXX-XXX.mirai.jar

插件本体，放入mirai-console的plugins文件夹。

- ConfigAndData.zip

解压后得到config、data合并至mirai-console的同名文件夹。若解压后没有config文件夹，说明该版本已不需要人工准备文件夹，插件会自动使用样例值创建。

### 3. 配置权限

本插件的指令受mirai-console权限系统管理，即权限配置结果会保存在`config/Console/PermissionService.yml`。

不是简单地将`<插件id>:*`授权给用户，而是有特殊用法，目的是：

- 指令分为用户级和管理级，可方便地区分授权
- 事件触发（例如戳一戳事件）和定时触发的功能，也可控制启用/禁用
- 若一个console里运行了两个bot，加入了同一个群，可控制仅其中一个bot响应本插件，另一个不响应本插件

**推荐的授权方法：**

使用[权限助手指令](./starter-functions/CharacterAdminHelperFunction.md)

因为这本身也是一种指令，所以调用者首先要拥有权限`<插件id>:command.权限助手`。

- 若想在console内使用权限助手指令，此时一定符合权限要求。
- 若想在聊天环境使用权限助手指令，则需[人工将上述权限授予某些对象](https://github.com/mamoe/mirai/blob/dev/docs/ConsoleTerminal.md#%E5%9C%A8%E7%BE%A4%E8%81%8A%E4%B8%AD%E4%BD%BF%E7%94%A8%E5%91%BD%E4%BB%A4-%E6%9D%83%E9%99%90%E6%8E%88%E4%BA%88)。

> 人工通过perm指令，将上述权限授予id为123456的用户
>
> /perm permit u114514 hundun.fleet.XXX:command.权限助手  

然后使用权限助手模块里的具体指令即可。

**候选授权方法：**

人工授权和管理各个特殊权限节点。需要阅读和理解[权限设计细节](./权限设计细节.md)，不推荐。

**从旧版权限配置升级：**

旧版指的是以前需要用户人工授权`<插件id>.cos:INSTANCE`

方法1：删除`config/Console/PermissionService.yml`里有关`<插件id>:`和`<插件id>.cos:`的部分，用新方法重新配置。

方法2：人工将`<插件id>.cos:INSTANCE`改成`<插件id>:cos.instance`，不推荐。

### 4. 启动和登录

启动mirai-console，在mirai-console里登录。
