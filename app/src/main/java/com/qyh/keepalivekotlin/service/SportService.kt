package com.qyh.keepalivekotlin.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.qyh.keepalivekotlin.utils.BroadcastManager
import java.util.*

/**
 * @author 邱永恒
 *
 * @time 2018/3/2  16:35
 *
 * @desc ${TODD}
 *
 */
const val TIME_ACTION = "RUNTIME"
class SportService : Service(){
    private lateinit var runTimer: Timer
    private var timeHour: Int = 0
    private var timeMin: Int = 0
    private var timeSec: Int = 0
    inner class SportBinder : Binder() {
        fun getService() : SportService{
            return this@SportService
        }
    }
    override fun onBind(intent: Intent?): IBinder {
        return SportBinder()
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }


     fun startRunTimer(handler: Handler) {
        val task = object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                timeSec++
                if (timeSec == 60) {
                    timeSec = 0
                    timeMin++
                }
                if (timeMin == 60) {
                    timeMin = 0
                    timeHour++
                }
                if (timeHour == 24) {
                    timeSec = 0
                    timeMin = 0
                    timeHour = 0
                }
                // 更新UI
                val message = Message()
                message.obj = "$timeHour : $timeMin : $timeSec"
                handler.sendMessage(message)
                BroadcastManager.getInstance().sendBroadcast(TIME_ACTION, "$timeHour : $timeMin : $timeSec")
            }
        }
        // 每隔1s更新一下时间
        runTimer = Timer()
        runTimer.schedule(task, 1000, 1000)
    }

    @SuppressLint("SetTextI18n")
     fun stopRunTimer(handler: Handler) {
        runTimer.cancel()
        timeHour = 0
        timeMin = 0
        timeSec = 0

        val message = Message()
        message.obj = "$timeHour : $timeMin : $timeSec"
        handler.sendMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        startService(Intent(applicationContext, SportService::class.java))
        BroadcastManager.getInstance().sendBroadcast(TIME_ACTION, "$timeHour : $timeMin : $timeSec")
    }
}