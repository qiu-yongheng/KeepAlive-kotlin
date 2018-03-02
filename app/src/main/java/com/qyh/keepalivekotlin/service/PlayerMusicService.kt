package com.qyh.keepalivekotlin.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.qyh.keepalivekotlin.R

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  10:19
 *
 * @desc ${TODD}
 *
 */
class PlayerMusicService : Service(){
    private lateinit var mediaPlayer: MediaPlayer
    companion object {
        val TAG = "PlayerMusicService"
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "启动播放音乐service")
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.yiqianlengyiye)
        mediaPlayer.isLooping = true // 设置播放器循环播放
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        // 在线程播放音乐
        Thread(Runnable { startPlayMusic() }).start()
        return START_STICKY
    }

    private fun startPlayMusic() {
        Log.d(TAG, "启动后台播放音乐")
        mediaPlayer.start()
    }

    private fun stopPlayMusic() {
        Log.d(TAG, "停止后台播放音乐")
        mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayMusic()
        Log.d(TAG, "停止播放音乐service")
        // 重启
        startService(Intent(applicationContext, PlayerMusicService::class.java))
    }
}