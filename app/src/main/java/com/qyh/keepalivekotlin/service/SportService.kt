package com.qyh.keepalivekotlin.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
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
    private lateinit var runTimer: Timer
    private var timeHour: Int = 0
    private var timeMin: Int = 0
    private var timeSec: Int = 0

    inner class SportBinder : Binder() {
        fun getService(): SportService {
            return this@SportService
        }
    }

    companion object {
        val TAG = "SportService"
        var startMs: Long = 0L
    }

    override fun onBind(intent: Intent?): IBinder {
        return SportBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    fun startRunTimer(handler: Handler) {
        val task = object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {

                // 更新UI
                val message = Message()
                val ms = System.currentTimeMillis() - startMs
                Log.d(TAG, "时间间隔: ${System.currentTimeMillis()}")
                val hms = ms2HMS(ms)
                message.obj = hms
                handler.sendMessage(message)
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
    fun stopRunTimer(handler: Handler) {
        runTimer.cancel()
        startMs = 0
        timeHour = 0
        timeMin = 0
        timeSec = 0

        val message = Message()
        message.obj = ms2HMS(0)
        handler.sendMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        startService(Intent(applicationContext, SportService::class.java))
        BroadcastManager.getInstance().sendBroadcast(TIME_ACTION, "$timeHour : $timeMin : $timeSec")
    }
}