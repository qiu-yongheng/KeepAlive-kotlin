# KeepAlive-kotlin
Android保活机制探索kotlin版

## 为什么会诞生这个项目
公司项目是户外运动软件, 保活处理自然是重中之重, 这个DEMO也是对Android保活机制的探索和思考, 顺便也练习下kotlin(这才是重点)

## 问题
1. 什么场景会杀进程?
2. 保活方法都有哪些
3. 保活的原理是什么?
4. 提升优先级的方式?

##  一. 杀进程场景
* 1, 2两种情况进程属于自然死亡, 我们的需求是在运动中, 应用推到后台, 可以持续记录运动记录, 所以我们要重点解决这两种情况下的保活问题
* 3-6属于用户主动杀死进程
* 7属于应用出现异常被系统杀死

序号|场景
--|--
1	|被lmk杀死
2	|成为空进程，缓存进程后被杀
3	|最近任务一键清理
4	|最近任务上滑被死
5	|使用Setting forceStop
6	|第三方应用如猎豹，360杀进程
7	|出现FC或者ANR


## 二. 保活方法
序号	|保活方法	|API限制	|效果&原理
--|--|--|--
1	|正常的开启前台Servce	|无限制 |启动前台Service伴随着Notification，如正在播放音乐、正在导航，系统默认给进程高优先级
2	|正常弹出悬浮窗	|无限制	|展示用户可见的窗口，如一键清理火箭，系统默认给进程高优先级
3	|设置persistent=true	|无限制	|成为系统常驻进程
4	|启动前台Service传入无意义Notification	|API<=17	|利用系统漏洞，不展示Notification但享受前台Service优先级待遇
5	|启动两个相同id前台Service，stop后者	|18<=API<=24	|同上，享受前台Service优先级待遇
6	|native层保活	|API<=23	|进程互保，通过文件锁监听死亡，如果死亡则拉起
7	|桌面放置一像素页面	|未知	|享受可见进程优先级待遇
8	|静态注册系统常用广播	|未知	|满足权限的前提下，收到广播前进程会被拉起
9	|进程Service/Provider互绑	|无限制	|效果并不好，forceStop下等场景无法存活，只是procState优先级很高
10	|AlarmManager/JobScheduler	|未知	|守护服务，如果被守护进程死亡则拉起
11	|账户同步	|未知	|Android原生机制会定期唤醒账户更新服务

## 三. 保活原理
系统按照优先级由低到高杀进程, 本质保活就是提升进程优先级, 所以我们要了解系统是怎么计算进程的优先级的

### updateOomAdjLocked(进程优先级更新)
#### updateOomAdjLocked (1) - 空进程、缓存进程
* 获取TOP_APP, 即前台Activity, 这个会在后面的updateOomAdjLocked方法中用到
* 重置所有UIDRecord
* 获取空进程, 缓存进程个数上限
* 计算出当前所有正在运行进程的adj并应用
* 如果到达了上限, 杀死: 空进程 -> 缓存进程 -> 进程是isolated并且没有在运行Service

#### updateOomAdjLocked (2) - 对进程内存trim
* 针对后台进程，对内存进行trim
* 初始化memFactor, 计算memFactor
* 如果当前进程memLevel小于当前的level，则需要对进程内存做trim
* 更新UidRecord变化

### computeOomAdjLocked(进程优先级的计算)
#### computeOomAdjLocked (1) - 进行特殊检查
> 这一步主要是对进程优先级更新时做一次检查；可以看到adj是进程优先级的主要衡量值

* 检查当前adj是否已经被计算
* 检查当前进程是否已经死亡
* 检查当前进程是否被设置了adj下限

#### computeOomAdjLocked (2) - 根据四大组件计算
> 以上代码对进程中的四大组件做了检查；进程优先级的计算秉承着从最重要的开始计算原则，但是如果在后面存在优先级更高的情况，则会取这其中的最小adj与procState作为最终的进程优先级


状态 | adj
--|--
是否有前台Activity |adj = ProcessList.FOREGROUND_APP_ADJ;
是否在运行intrumentation |adj = ProcessList.FOREGROUND_APP_ADJ;
是否正在接收广播 |adj = ProcessList.FOREGROUND_APP_ADJ;
是否正在执行Service |adj = ProcessList.FOREGROUND_APP_ADJ;
都不符合则暂时为空 |adj = cacheAdj;
如果Activity可见 |adj = ProcessList.VISIBLE_APP_ADJ;
如果在pause状态 | adj = ProcessList.PERCEPTIBLE_APP_ADJ;
如果在stop状态 | adj = ProcessList.PERCEPTIBLE_APP_ADJ;

#### computeOomAdjLocked (3) - 特殊进程
> 计算优先级的过程中判断了一些特殊进程

状态 | adj
--|--
检查进程是否为可察觉 | adj = ProcessList.PERCEPTIBLE_APP_ADJ;
是否为heavy-weight进程 | adj = ProcessList.HEAVY_WEIGHT_APP_ADJ;
是否为home进程 | adj = ProcessList.HOME_APP_ADJ;
进程是否之前展示过 | adj = ProcessList.PREVIOUS_APP_ADJ;
是否为备份进程 | adj = ProcessList.BACKUP_APP_ADJ;

#### computeOomAdjLocked (4) - Service/Provider绑定
* 遍历所有运行中的Service，进行计算
* 对绑定的Service进程优先级进行判断
* 对绑定的provider进程优先级进行判断
* 对adj作出最后的调整

### 优先级计算流程图
![](http://oyknau7uh.bkt.clouddn.com/process_compute.jpg)

### 总结
1. 负数的是系统进程, 优先级最高
2. adj值越低, 进程优先级越高

adj|值|场景
--|--|--
NATIVE_ADJ	|-17|	native进程，framework中未涉及
SYSTEM_ADJ	|-16|	系统进程，也就是system_server
PERSISTENT_PROC_ADJ	|-12|	系统常驻进程，如telephony, systemui
PERSISTENT_SERVICE_ADJ	|-11| 1) 被系统进程或常驻进程绑定 2) 设置了BIND_ABOVE_CLIENT的flag
FOREGROUND_APP_ADJ	|0|	1) 当前进程正在显示Activity 2) instrumentation正在运行 3) 正在接收广播 4) 正在运行Service 5) provider被其它进程使用
VISIBLE_APP_ADJ	|1|	该APP中有可见的Activity
PERCEPTIBLE_APP_ADJ	|2|	 1) Activity正在或已经暂停 2) Activity正在Stop
BACKUP_APP_ADJ	|3|	备份进程
HEAVY_WEIGHT_APP_ADJ	|4|	heavy weight进程
SERVICE_ADJ	|5|	30分钟内Service被启动
HOME_APP_ADJ	|6|	home进程
PREVIOUS_APP_ADJ	|7|	运行过上一个显示的Activity
SERVICE_B_ADJ	|8|	运行Service未显示UI
CACHED_APP_MIN_ADJ	|9|	空进程最小adj
CACHED_APP_MAX_ADJ	|15|	app.thread=NULL
UNKNOWN_ADJ	|16|	adj未知

## 4. 提升优先级的方式?
让进程的优先级为FOREGROUND_APP_ADJ
#### 1. 当前进程正在显示Activity
> 注册锁屏广播, 当锁屏时, Activity设置启动模式为singleTask, 启动Activity到栈顶


#### 2. 正在接受广播
> 静态注册系统广播, 如网络状态广播, 安装卸载广播, 当接收到系统广播, 检查进程是否存活

KeepAliveReceiver
```
/**
 * @author 邱永恒
 *
 * @time 2018/3/1  13:49
 *
 * @desc
 *  监听系统广播，复活进程
 *  (1) 网络变化广播
 *  (2) 屏幕解锁广播(不能使用静态注册)
 *  (3) 应用安装卸载广播
 *  (4) 开机广播
 */
class KeepAliveReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "KeepAliveReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "AliveBroadcastReceiver---->接收到的系统广播：" + action!!)
        getNetworkBroadcast(context!!, intent)
        if (SystemUtils.isAppAlive(context, Contants.PACKAGE_NAME)) {
            Log.i(TAG, "AliveBroadcastReceiver---->APP还是活着的")
            return
        }
        val intentAlive = Intent(context, SportActivity::class.java)
        intentAlive.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intentAlive)
        Log.i(TAG, "AliveBroadcastReceiver---->复活进程(APP)")
    }

    private fun getNetworkBroadcast(context: Context, intent: Intent) {
        val action = intent.action
        // wifi状态改变
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            when (wifiState) {
                WifiManager.WIFI_STATE_DISABLED -> Toast.makeText(context, "wifi关闭", Toast.LENGTH_SHORT).show()
                WifiManager.WIFI_STATE_ENABLED -> Toast.makeText(context, "wifi开启", Toast.LENGTH_SHORT).show()
                else -> {
                }
            }
        }
        // 连接到一个有效wifi路由器
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == action) {
            val parcelableExtra = intent.getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
            if (null != parcelableExtra) {
                val networkInfo = parcelableExtra as NetworkInfo
                val state = networkInfo.state
                val isConnected = state == NetworkInfo.State.CONNECTED
                if (isConnected) {
                    Toast.makeText(context, "设备连接到一个有效WIFI路由器", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 监听网络连接状态，包括wifi和移动网络数据的打开和关闭
        // 由于上面已经对wifi进行处理，这里只对移动网络进行监听(该方式检测有点慢)
        // 其中，移动网络--->ConnectivityManager.TYPE_MOBILE；
        //       Wifi--->ConnectivityManager.TYPE_WIFI
        //       不明确类型：ConnectivityManager.EXTRA_NETWORK_INFO
        if (ConnectivityManager.CONNECTIVITY_ACTION == action) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (gprs.isConnected) {
                Toast.makeText(context, "移动网络打开", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "移动网络关闭", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
```
#### 3. 正在运行service
> 双进程守护, 开启前台service, JobService, service播放无声音频

SportService
```
package com.qyh.keepalivekotlin.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.qyh.keepalivekotlin.DaemonConnection
import com.qyh.keepalivekotlin.R
import com.qyh.keepalivekotlin.utils.BroadcastManager
import com.qyh.keepalivekotlin.utils.Contants.Companion.TIME_ACTION
import java.util.*

/**
 * @author 邱永恒
 *
 * @time 2018/3/2  16:35
 *
 * @desc ${TODD}
 *
 */
class SportService : Service() {
    private val binder : SportBinder by lazy { SportBinder() }
    private val serviceConnection : SportServiceConnection by lazy { SportServiceConnection() }
    private lateinit var runTimer: Timer
    private var timeHour: Int = 0
    private var timeMin: Int = 0
    private var timeSec: Int = 0
    companion object {
        val TAG = "SportService"
        var startMs: Long = 0L
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("运动中")
                .setContentText("正在运动...")
        startForeground(200, builder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 绑定本地service
        bindService(Intent(this, DaemonService::class.java), serviceConnection, Context.BIND_IMPORTANT)
        Log.d(TAG, "$TAG onStartCommand")
        return START_STICKY
    }

    fun startRunTimer() {
        val task = object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                // 更新UI
                val ms = System.currentTimeMillis() - startMs
                Log.d(TAG, "时间间隔: ${System.currentTimeMillis()}")
                val hms = ms2HMS(ms)
                BroadcastManager.getInstance().sendBroadcast(TIME_ACTION, hms)
            }
        }
        // 每隔1s更新一下时间
        startMs = System.currentTimeMillis()
        Log.d(TAG, "开始时间: $startMs")
        runTimer = Timer()
        runTimer.schedule(task, 0, 1000)
    }

    fun ms2HMS(ms: Long): String {
        val m = ms / 1000
        val hour = m / 3600
        val mint = m % 3600 / 60
        val sed = m % 60
        return String.format("%02d:%02d:%02d", hour, mint, sed)
    }

    @SuppressLint("SetTextI18n")
    fun stopRunTimer() {
        runTimer.cancel()
        startMs = 0
        timeHour = 0
        timeMin = 0
        timeSec = 0

        val ms2HMS = ms2HMS(0)
        BroadcastManager.getInstance().sendBroadcast(TIME_ACTION, ms2HMS)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "$TAG onDestroy")
        startService(Intent(applicationContext, SportService::class.java))
    }

    inner class SportBinder : DaemonConnection.Stub() {
        override fun startRunTimer() {
            this@SportService.startRunTimer()
        }

        override fun stopRunTimer() {
            this@SportService.stopRunTimer()
        }

        override fun getProcessName(): String {
            return "SportService"
        }
    }

    inner class SportServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            this@SportService.startService(Intent(this@SportService, DaemonService::class.java))
            this@SportService.bindService(Intent(this@SportService, DaemonService::class.java), serviceConnection, Context.BIND_IMPORTANT)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val daemonConnection = DaemonConnection.Stub.asInterface(service)
            Log.d(TAG, "连接远程service: ${daemonConnection.processName}")
        }
    }
}
```

DaemonService
```
package com.qyh.keepalivekotlin.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.qyh.keepalivekotlin.DaemonConnection
import com.qyh.keepalivekotlin.R

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  8:53
 *
 * @desc 后台守护service, 这个Service尽量要轻，不要占用过多的系统资源，否则系统在资源紧张时，照样会将其杀死
 *
 */
class DaemonService : Service() {
    private val binder : DaemonBinder by lazy { DaemonBinder() }
    private val serviceConnection : DaemonServiceConnection by lazy { DaemonServiceConnection() }
    companion object {
        val TAG = "DaemonService"
        val NOTICE_ID = 100
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 绑定本地service
        bindService(Intent(this, SportService::class.java), serviceConnection, Context.BIND_IMPORTANT)
        // 启动前台进程
        startForeground()
        return START_STICKY
    }

    private fun startForeground() {
        val builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("程序保活")
                .setContentText("守护service正在运行中...")
        startForeground(NOTICE_ID, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消状态栏广播
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTICE_ID)
    }

    class DaemonBinder : DaemonConnection.Stub() {
        override fun startRunTimer() {

        }

        override fun stopRunTimer() {

        }

        override fun getProcessName(): String {
            return "DaemonService"
        }
    }

    inner class DaemonServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            this@DaemonService.startService(Intent(this@DaemonService, SportService::class.java))
            this@DaemonService.bindService(Intent(this@DaemonService, SportService::class.java), serviceConnection, Context.BIND_IMPORTANT)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val daemonConnection = DaemonConnection.Stub.asInterface(service)
            Log.d(TAG, "连接本地service: ${daemonConnection.processName}")
        }
    }
}
```

AIDL
```
// DeamonConnection.aidl
package com.qyh.keepalivekotlin;

// Declare any non-default types here with import statements

interface DaemonConnection {
    String getProcessName();
    void startRunTimer();
    void stopRunTimer();
}
```



