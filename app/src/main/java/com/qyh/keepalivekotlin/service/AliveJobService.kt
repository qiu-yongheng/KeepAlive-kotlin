package com.qyh.keepalivekotlin.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import com.qyh.eyekotlin.utils.showToast
import com.qyh.keepalivekotlin.SportActivity
import com.qyh.keepalivekotlin.utils.Contants
import com.qyh.keepalivekotlin.utils.SystemUtils

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  9:03
 *
 * @desc 接收系统定时任务的service
 *
 */
class AliveJobService : JobService(){
    var handle = Handler({msg ->
        // 具体任务逻辑
        if (SystemUtils.isAppAlive(applicationContext, Contants.PACKAGE_NAME)) {
            showToast("APP正在运行...")
        } else {
            val intent = Intent(applicationContext, SportActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Log.d(TAG, "APP杀死重启中")
            showToast("APP杀死重启中")
        }
        // 任务执行完后记得调用jobFinished通知系统释放相关资源
        jobFinished(msg.obj as JobParameters, false)
        true
    })

    companion object {
        // 告知编译器，这个变量不能被优化
        @Volatile var aliveJobService : AliveJobService? = null
        val TAG = "AliveJobService"
        val MESSAGE_ID_TASK = 0x01

        fun isJobServiceAlive() : Boolean {
            return aliveJobService != null
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob()")
        handle.removeMessages(MESSAGE_ID_TASK)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob()")
        aliveJobService = this
        // 返回false，系统假设这个方法返回时任务已经执行完毕；
        // 返回true，系统假定这个任务正要被执行
        // 创建Message
        val msg = Message.obtain(handle, MESSAGE_ID_TASK, params)
        handle.sendMessage(msg)
        return true
    }
}