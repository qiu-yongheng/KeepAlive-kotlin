package com.qyh.keepalivekotlin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.qyh.keepalivekotlin.SinglePixelActivity
import java.lang.ref.WeakReference

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  11:19
 *
 * @desc 1像素悬浮窗管理类
 *
 */
class ScreenManager private constructor(var context: WeakReference<Context>){
    var activityWeakR: WeakReference<Activity>? = null
    companion object {
        val TAG = "ScreenManager"
        private var instance: ScreenManager? = null
        fun getInstance(context: WeakReference<Context>) : ScreenManager {
            if (instance == null) {
                synchronized(ScreenManager::class) {
                    if (instance == null) {
                        instance = ScreenManager(context)
                    }
                }
            }
            return instance!!
        }
    }

    /**
     * 获得SinglePixelActivity的引用
     */
    fun setSingleActivity(activity: Activity) {
        activityWeakR = WeakReference(activity)
    }

    /**
     * 启动SinglePixelActivity
     */
    fun startActivity() {
        val intent = Intent(context.get()!!, SinglePixelActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.get()?.startActivity(intent)
    }

    /**
     * 结束SinglePixelActivity
     */
    fun finishActivity() {
        activityWeakR?.get()?.finish()
    }
}