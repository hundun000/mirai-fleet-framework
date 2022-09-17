### 其他：简单的功能或实验性的功能

#### 【群消息事件】复读

当群里连续3句发言相同时（包括发送相同表情/图片），插件会让bot也会发送一次该发言。

#### 【指令-管理级】立刻群聊

**<子指令>: 立刻群聊**  
**<指令参数1>: 参数解析group**  
**<指令参数2>: 消息内容**    

插件会让bot立刻在对象群发送消息内容。    

[参数解析group说明](https://github.com/mamoe/mirai/blob/dev/docs/ConsoleTerminal.md#%E6%8C%87%E4%BB%A4%E5%8F%82%E6%95%B0%E6%99%BA%E8%83%BD%E8%A7%A3%E6%9E%90)

>  -> /<主指令名> 立刻群聊 12345 hello  
>  (群12345) <- hello

#### 【指令-管理级】立刻私聊

**<子指令>: 立刻私聊**  
**<指令参数1>: 参数解析friend**  
**<指令参数2>: 消息内容 **  

插件会让bot立刻向对象用户发送消息内容。

[参数解析friend说明](https://github.com/mamoe/mirai/blob/dev/docs/ConsoleTerminal.md#%E6%8C%87%E4%BB%A4%E5%8F%82%E6%95%B0%E6%99%BA%E8%83%BD%E8%A7%A3%E6%9E%90)

>  -> /<主指令名> 立刻私聊 12345 hello  
>  (好友12345) <- hello