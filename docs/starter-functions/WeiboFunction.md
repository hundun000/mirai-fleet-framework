### 微博功能模块

#### 【配置】文案模板

手动编辑`config\hundun.fleet.amiya\WeiboFunction\WeiboConfig.json`的`noNewBlogMessageTemplate`、`newBlogMessageTemplate`、`summaryBlogMessageTemplate`。

各个文案模板用于何处，将在后文用到时给出。

#### 【配置】订阅关系

手动编辑`config\hundun.fleet.amiya\WeiboFunction\WeiboConfig.json`的`listenConfig`。

微博输出格式枚举说明：

|输出格式|说明|
|----|----|
|NO_IMAGE|不输出图片|
|FIRST_IMAGE|最多输出一张图片|
|ALL_IMAGE|输出所有图片|

根据账号配图特点，选择微博输出格式。例如：
- 某个账号主要发的是9宫格拼图，而bot输出的图片无法保持九宫格形状，输出的意义不大，可以选择`NO_IMAGE`；
- 某个账号主要发的是单张图，有时发9宫格，可以选择`FIRST_IMAGE`；
- 某个账号主要发的是多张图，且每张图都需要输出，可以选择`ALL_IMAGE`；

#### 【指令】重新载入订阅关系

**<子指令>: 刷新微博订阅**  

【注意】第一次启用该插件，或修改`WeiboConfig.json`添加了新的微博uid后需要执行一次。在miral-console内执行该指令即可。

> -> /<主指令名> 刷新微博订阅  
> <- 已刷新

#### 【指令】查看最新微博

**<子指令>: 最新微博**  
**<指令参数1>: [可选]微博用户名**

若不带`微博用户名`参数，则回复该角色订阅的所有微博用户的最新一条微博的更新时间

> -> /<主指令名> 最新微博  
> <- 来自：明日方舟Arknights，最新的饼的时间是：2021-11-30T15:00:06  
>    来自：明日方舟朝陇山，最新的饼的时间是：2021-12-01T10:00:01  
>    来自：泰拉记事社，最新的饼的时间是：2021-11-28T11:00:03  

此时文案模板来自`WeiboConfig.summaryBlogMessageTemplate`或`WeiboConfig.noNewBlogMessageTemplate`。

若带`微博用户名`参数，则回复指定微博用户的最新一条微博的内容

> -> /<主指令名> 最新微博 明日方舟朝陇山  
> <- 新饼！来自：明日方舟朝陇山 2021-12-01T10:00:01  
>    
>   
> 今天是十二月的第一天！不知道2021年的最后一个月，大家是忙碌还是清闲呢~   
> 以及1张图片。   

#### 【指令】查看订阅关系

**<子指令>: 查询微博订阅**  

简单打印微博订阅关系配置，仅作为调试。

> -> /<主指令名> 查询微博订阅  
> <- {3837704366=FIRST_IMAGE}

#### 【自动】推送微博

插件定时检查已订阅的微博，若有新微博，则会自动发送到群里。

> <- 新饼！来自：明日方舟朝陇山 2021-12-01T10:00:01  
>   
>   
> 今天是十二月的第一天！不知道2021年的最后一个月，大家是忙碌还是清闲呢~   
> 以及1张图片。  

此时文案模板来自`WeiboConfig.newBlogMessageTemplate`。

#### 【指令-Debug级】changeTopCardCreateTime

**<主指令名>: 代入 <角色名>WeiboFunctionDebug**  
**<子指令>: 固定 changeTopCardCreateTime**  
**<指令参数1>: 微博uid**

将该uid的最新一条微博的发送时间修改（伪造）为当前时刻。因此这条微博将在下一次检查中被判定为新微博，然后被自动发送到群里。

> -> /<主指令名> changeTopCardCreateTime 3837704366  
> <- OK  
> (等到下一次检查)  
> <- 新饼！……


