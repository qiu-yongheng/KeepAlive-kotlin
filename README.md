# KeepAlive-kotlin
Android保活机制探索kotlin版
1. 监听锁屏状态service(个人觉得可以作为守护进程)
```
LockScreenService: 只做一件事, 注册屏幕锁屏广播, 在锁屏时, 将SportActivity启动到栈顶
ScreenReceiver: 封装屏幕锁屏广播, 启动和注销
```
2. 作业调度器(会被杀死)
3. 真正工作的service(必须设置前台service, 不然会被杀死)
4. 静态注册网络状态, 应用安装卸载广播, 当收到广播时, 检查进程是否已被杀死
