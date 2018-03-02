package com.qyh.keepalivekotlin

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
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
import com.qyh.keepalivekotlin.service.SportService.SportBinder
import com.qyh.keepalivekotlin.utils.JobSchedulerManager
import kotlinx.android.synthetic.main.activity_sport.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  8:45
 *
 * @desc 运动界面:
 *       1. 当锁屏时, start SportActivity
 *       2. 开启一个后台Service, 执行定时任务
 *
 */
class SportActivity : AppCompatActivity(), View.OnClickListener {
//    private val screenReceiverUtils: ScreenReceiverUtils by lazy { ScreenReceiverUtils(this) } // 监听屏幕广播工具类
//    private val screenManager: ScreenManager by lazy { ScreenManager.getInstance(WeakReference(this)) } // 1像素显示管理类
    private val jobManager: JobSchedulerManager by lazy { JobSchedulerManager.getInstance(WeakReference(this)) }
    private var isRunning = false
    private var timeHour: Int = 0
    private var timeMin: Int = 0
    private var timeSec: Int = 0
    private var runTimer: Timer? = null
    private var sportService: SportService? = null
    val handler = Handler({msg ->
        val time = msg.obj as String
        tv_run_time.text = time
        true
    })
    val serviceConnected = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val sportBinder = service as SportBinder
            sportService = sportBinder.getService()
        }

    }

//    private val screenListener = object : ScreenReceiverUtils.ScreenStatusListener {
//        override fun onScreenOn() {
//            // 亮屏, 移除"1像素悬浮窗"
//            // screenManager.finishActivity()
//        }
//
//        override fun onScreenOff() {
//            // 锁屏, 将SportActivity切换到可见模式
//            startActivity(Intent(this@SportActivity, SportActivity::class.java))
//            // 或者启动一个"1像素"窗口
//            // screenManager.startActivity()
//        }
//
//        override fun onUserPresent() {
//            // TODO: 解锁
//        }
//    }
    companion object {
        val TAG = "SportActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        setContentView(R.layout.activity_sport)
        // 1. 注册锁屏广播监听器
//        screenReceiverUtils.setScreenStatusListener(screenListener)
        startService(Intent(this, LockScreenService::class.java))
        // 2. 启动系统任务
        jobManager.startJobScheduler()
        // 3. 启动运动数据服务
        val intent = Intent(this, SportService::class.java)
        startService(intent)
        bindService(intent, serviceConnected, Context.BIND_AUTO_CREATE)
        initListener()
    }

    private fun initListener() {
        btn_start_sport.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_start_sport -> {
                if (!isRunning) {
                    btn_start_sport.text = "停止运动"
                    // 1. 启动计时器
                    //startRunTimer()
                    sportService?.startRunTimer(handler)
                    // 2. 启动前台Service
                    startDaemonService()
                    // 3. 启动播放音乐Service
                    startPlayMusicService()
                } else {
                    btn_start_sport.text = "开始运动"
                    //stopRunTimer()
                    sportService?.stopRunTimer(handler)
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

    private fun startRunTimer() {
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
                runOnUiThread { tv_run_time.text = "$timeHour : $timeMin : $timeSec" }
            }
        }
        // 每隔1s更新一下时间
        runTimer = Timer()
        runTimer?.schedule(task, 1000, 1000)
    }

    private fun stopPlayMusicService() {
        stopService(Intent(this, PlayerMusicService::class.java))
    }

    private fun stopDaemonService() {
        stopService(Intent(this, DaemonService::class.java))
    }

    @SuppressLint("SetTextI18n")
    private fun stopRunTimer() {
        runTimer?.cancel()
        runTimer = null
        timeHour = 0
        timeMin = 0
        timeSec = 0
        tv_run_time.text = "$timeHour : $timeMin : $timeSec"
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
        //stopRunTimer()
        // 停止监听锁屏广播
        // screenReceiverUtils.stopScreenStatusListener()
        stopService(Intent(applicationContext, LockScreenService::class.java))
        sportService?.stopRunTimer(handler)
        unbindService(serviceConnected)
    }

}