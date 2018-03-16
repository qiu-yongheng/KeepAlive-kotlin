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