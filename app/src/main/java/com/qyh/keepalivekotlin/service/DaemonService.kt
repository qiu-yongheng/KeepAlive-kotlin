package com.qyh.keepalivekotlin.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
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
    companion object {
        val TAG = "DaemonService"
        val NOTICE_ID = 100
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 显示状态栏广播
        val builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("程序保活")
                .setContentText("守护service正在运行中...")
        startForeground(NOTICE_ID, builder.build())

        // 如果觉得常驻通知栏体验不好
        // 可以通过启动CancelNoticeService，将通知移除，oom_adj值不变
        // startService(Intent(this, CancelNoticeService::class.java))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // kill后会被重启，但是重启后调用onStarCommand（）传进来的Intent参数为null，说明被kill的时候没有保存Intent
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消状态栏广播
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTICE_ID)

        // 重启service
        startService(Intent(applicationContext, DaemonService::class.java))
    }
}