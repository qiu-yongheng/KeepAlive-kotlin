package com.qyh.keepalivekotlin.utils

import android.annotation.TargetApi
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.qyh.keepalivekotlin.service.AliveJobService
import java.lang.ref.WeakReference

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  12:46
 *
 * @desc JobScheduler管理类，单例模式
 *       执行系统任务
 *
 */
class JobSchedulerManager private constructor(val context: WeakReference<Context>) {
    var jobScheduler: JobScheduler? = null

    init {
        // 1. 创建JobScheduler
        jobScheduler = context.get()?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    companion object {
        private val JOB_ID = 1
        private var instance: JobSchedulerManager? = null
        fun getInstance(context: WeakReference<Context>): JobSchedulerManager {
            if (instance == null) {
                synchronized(JobSchedulerManager::class) {
                    if (instance == null) {
                        instance = JobSchedulerManager(context)
                    }
                }
            }
            return instance!!
        }
    }

    /**
     *
     */
    @TargetApi(21)
    fun startJobScheduler() {
        // 如果JobService已经启动或API<21，返回
        if (AliveJobService.isJobServiceAlive() || isBelowLOLLIPOP()) {
            return
        }
        // 2. 构建JobInfo对象，传递给JobSchedulerService(绑定ID和绑定实现了JobService的之类的组件名)
        val builder = JobInfo.Builder(JOB_ID, ComponentName(context.get()!!, AliveJobService::class.java))
        // 3. 设置条件
        // 设置每5秒执行一下任务
        builder.setPeriodic(5000)
        // 设置设备重启时，执行该任务
        builder.setPersisted(true)
        // 当插入充电器，执行该任务
        builder.setRequiresCharging(true)
        val info = builder.build()
        // 开始定时执行该系统任务
        jobScheduler?.schedule(info)
    }

    /**
     * 没有回调AliveJobService.onStopJob(), 目前不知原因
     */
    @TargetApi(21)
    fun stopJobScheduler() {
        if (isBelowLOLLIPOP()) return
        jobScheduler?.cancelAll()
    }

    private fun isBelowLOLLIPOP(): Boolean {
        // API< 21
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
    }
}