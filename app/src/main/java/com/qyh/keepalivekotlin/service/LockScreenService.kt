package com.qyh.keepalivekotlin.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.qyh.eyekotlin.utils.showToast
import com.qyh.keepalivekotlin.SportActivity
import com.qyh.keepalivekotlin.receiver.ScreenReceiverUtils
import com.qyh.keepalivekotlin.utils.ScreenManager
import java.lang.ref.WeakReference

/**
 * @author 邱永恒
 *
 * @time 2018/3/2  11:04
 *
 * @desc ${TODD}
 *
 */
class LockScreenService : Service() {
    /**
     * 监听屏幕广播工具类
     */
    private val screenReceiverUtils: ScreenReceiverUtils by lazy { ScreenReceiverUtils(this) }
    /**
     * 1像素显示管理类
     */
    private val screenManager: ScreenManager by lazy { ScreenManager.getInstance(WeakReference(this)) }
    /**
     * 屏幕广播监听器
     */
    private val screenListener = object : ScreenReceiverUtils.ScreenStatusListener {
        override fun onScreenOn() {
            // 亮屏, 移除"1像素悬浮窗"
//            screenManager.finishActivity()
            // 显示自定义锁屏
//            val intent = Intent(this@LockScreenService, LockScreenActivity::class.java)
            // Activity要存在于activity的栈中，而Service在启动activity时必然不存在一个activity的栈，所以要新起一个栈，并装入启动的activity。
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 为了避免在最近使用程序列表出现Service所启动的Activity
//            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
//            startActivity(intent)
        }

        override fun onScreenOff() {
            // 锁屏, 将SportActivity切换到可见模式
            val intent = Intent(this@LockScreenService, SportActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            // 或者启动一个"1像素"窗口
//            screenManager.startActivity()
        }

        override fun onUserPresent() {
            showToast("解锁")
        }
    }

    companion object {
        val TAG = "LockScreenService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "$TAG onStartCommand")
        screenReceiverUtils.setScreenStatusListener(screenListener)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "$TAG onDestroy")
        startService(Intent(applicationContext, LockScreenService::class.java))
        screenReceiverUtils.stopScreenStatusListener()
    }
}