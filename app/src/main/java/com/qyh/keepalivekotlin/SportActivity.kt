package com.qyh.keepalivekotlin

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.qyh.eyekotlin.utils.showToast
import com.qyh.keepalivekotlin.service.DaemonService
import com.qyh.keepalivekotlin.service.LockScreenService
import com.qyh.keepalivekotlin.service.PlayerMusicService
import com.qyh.keepalivekotlin.service.SportService
import com.qyh.keepalivekotlin.utils.BroadcastManager
import com.qyh.keepalivekotlin.utils.Contants
import com.qyh.keepalivekotlin.utils.JobSchedulerManager
import kotlinx.android.synthetic.main.activity_sport.*
import java.lang.ref.WeakReference

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  8:45
 *
 * @desc 运动界面:
 *       1. 当锁屏时, start SportActivity
 *       2. 开启一个后台Service, 执行定时任务
 */
class SportActivity : AppCompatActivity(), View.OnClickListener {
    private var daemonConnection: DaemonConnection? = null
    /**
     * 任务调度器
     */
    private val jobManager: JobSchedulerManager by lazy { JobSchedulerManager.getInstance(WeakReference(this)) }
    /**
     * 是否正在运动的标志
     */
    private var isRunning = false
    /**
     * 与SportService绑定
     */
    private val serviceConnected = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            daemonConnection = DaemonConnection.Stub.asInterface(service)
        }
    }
    /**
     * 伴生对象
     */
    companion object {
        val TAG = "SportActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        setContentView(R.layout.activity_sport)
        // 1. 启动锁屏广播监听器
        startService(Intent(this, LockScreenService::class.java))
        // 2. 启动作业调度器
        jobManager.startJobScheduler()
        // 3. 启动运动数据服务
        val intent = Intent(this, SportService::class.java)
        startService(intent)
        bindService(intent, serviceConnected, Context.BIND_IMPORTANT)
        initListener()
    }

    private fun initListener() {
        btn_start_sport.setOnClickListener(this)
        BroadcastManager.getInstance().addAction(Contants.TIME_ACTION, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getStringExtra("String")
                tv_run_time.text = time
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_start_sport -> {
                if (!isRunning) {
                    btn_start_sport.text = "停止运动"
                    // 1. 启动计时器
                    daemonConnection?.startRunTimer()
                    // 2. 启动前台Service
                    startDaemonService()
                    // 3. 启动播放音乐Service
                    startPlayMusicService()
                } else {
                    btn_start_sport.text = "开始运动"
                    daemonConnection?.stopRunTimer()
                    stopService(Intent(this, SportService::class.java))
                    stopDaemonService()
                    stopPlayMusicService()
                }
                isRunning = !isRunning
            }
        }
    }

    private fun startPlayMusicService() {
        startService(Intent(this, PlayerMusicService::class.java))
    }

    private fun startDaemonService() {
        startService(Intent(this, DaemonService::class.java))
    }

    private fun stopPlayMusicService() {
        stopService(Intent(this, PlayerMusicService::class.java))
    }

    private fun stopDaemonService() {
        stopService(Intent(this, DaemonService::class.java))
    }

    /**
     * 禁用返回键
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                showToast("正在跑步, 不能退出, 请按home键返回")
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "$TAG onDestroy")
        unbindService(serviceConnected)
    }
}