package com.qyh.keepalivekotlin.utils

import android.app.ActivityManager
import android.content.Context

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  9:33
 *
 * @desc ${TODD}
 *
 */
class SystemUtils {
    companion object {
        /**
         * 判断本应用是否存活
         * 如果需要判断本应用是否在后台还是前台用getRunningTask
         */
        fun isAppAlive(context: Context, packageName : String) : Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // 获取所有正在运行的app
            val appProcessInfoList = activityManager.runningAppProcesses
            // 遍历，进程名即包名
            return appProcessInfoList.any { packageName == it.processName }
        }

    }
}