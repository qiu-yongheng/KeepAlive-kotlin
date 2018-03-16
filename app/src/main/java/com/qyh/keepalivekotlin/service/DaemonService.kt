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