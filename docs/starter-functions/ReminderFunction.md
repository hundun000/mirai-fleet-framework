### 事项提醒功能模块

#### 【配置】自定义提醒内容

方法：

1）手动编辑（重启后生效）`data\hundun.fleet.amiya\ReminderFunction\repositories\ReminderListRepository.json`   
2）指令创建，详见后文。

json文件数据结构说明：

|字段名|示例值|含义|
|---|---|---|
|cron|* 0 22 ? * 1|执行的时间条件。一个 [cron表达式](https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm)|
|count|null|执行次数条件。无限次对应null|
|text|现在是周日晚上10点|提醒内容。即bot会发送的文本|

【注意】本项目的cron实现使用quartz库。特别地，其对于星期几的数值表示和某些其他标准中的表示不同，注意不要混淆。例如周日的的数值表示是1（而不是某些其他标准中的0或7）。具体如下，
```
("SUN", 1);
("MON", 2);
("TUE", 3);
("WED", 4);
("THU", 5);
("FRI", 6);
("SAT", 7);
```

### 【定时】整点报时

定时触发。报时文本来自配置文件。

>  <- 早上九点到了。嗯？啊，是长门！喂！长~门！……嗯？在哪儿遇见过？那当然是！……等……奇怪？那个……是在哪儿来着……？

配置方法：

手动编辑（重启后生效）`config\hundun.fleet.amiya\ReminderFunction\HourlyChatConfig.json`

#### 【指令】查看整点报时配置

**<子指令>: 查询报时**  

简单打印报时配置，仅作为调试。

>  -> /<角色名> 查询报时  
> <- HourlyChatConfig(chatTexts={0=……, 1=……, ……})

#### 【定时】自定义提醒

定时触发。提醒任务来自data文件，见配置说明。也可通过指令创建。

>  <- 现在是周日晚上10点。请博士记得完成本周剿灭作战。

#### 【指令】创建提醒任务

**<子指令>: 创建提醒**  
**<指令参数1>: 时间条件。用~代替cron表达式中的空格**  
**<指令参数2>: 执行次数条件。值域：x次，无限次**  
**<指令参数3>: 提醒内容。即bot会发送的文本**  

>  -> /<角色名> 创建提醒 \*\~0\~22\~?\~\*\~1 无限次 现在是周日晚上10点。请博士记得完成本周剿灭作战。  
>  <- OK

【注意】插件只会每分钟（的某一个毫秒）检查一次提醒任务，所以cron表达式中的毫秒条件只应填`*`。

#### 【指令】查看所有提醒任务

**<子指令>: 查询提醒**  

>  -> /<角色名> 查询提醒  
>  <- items:  
>     id:0	ReminderItem(count=null, text=现在是周日晚上10点。请博士记得完成本周剿灭作战。, cron=* 0 22 ? * 7)

#### 【指令】删除提醒任务

**<子指令>: 删除提醒**  
**<指令参数1>: id。即查询提醒里看到的id**

>  -> /<角色名> 删除提醒 0  
>  <- OK