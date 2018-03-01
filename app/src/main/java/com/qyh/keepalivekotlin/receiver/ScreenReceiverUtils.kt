package com.qyh.keepalivekotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  10:38
 *
 * @desc 静态监听锁屏、解锁、开屏广播
 *       a) 当用户锁屏时，将SportsActivity置于前台，同时开启1像素悬浮窗；
 *       b) 当用户解锁时，关闭1像素悬浮窗；
 */
class ScreenReceiverUtils(private val context: Context) {
    companion object {
        val TAG = "ScreenReceiverUtils"
        var listener: ScreenStatusListener? = null
    }
    private val screenReceiver: ScreenReceiver by lazy {
        ScreenReceiver()
    }


    fun setScreenStatusListener(screenListener: ScreenStatusListener) {
        listener = screenListener
        // 动态启动广播接收器
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(screenReceiver, filter)
    }

    fun stopScreenStatusListener() {
        context.unregisterReceiver(screenReceiver)
    }

    class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "ScreenReceiver --> 监听到系统广播: " + action)
            when (action) {
                Intent.ACTION_SCREEN_ON -> listener?.onScreenOn() // 开屏
                Intent.ACTION_SCREEN_OFF -> listener?.onScreenOff() // 锁屏
                Intent.ACTION_USER_PRESENT -> listener?.onUserPresent() // 解锁
            }
        }
    }

    interface ScreenStatusListener {
        fun onScreenOn()
        fun onScreenOff()
        fun onUserPresent()
    }
}
