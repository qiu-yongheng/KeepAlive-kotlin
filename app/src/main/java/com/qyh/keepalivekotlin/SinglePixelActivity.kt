package com.qyh.keepalivekotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import com.qyh.keepalivekotlin.utils.Contants
import com.qyh.keepalivekotlin.utils.ScreenManager
import com.qyh.keepalivekotlin.utils.SystemUtils
import java.lang.ref.WeakReference

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  11:50
 *
 * @desc 1像素Activity
 *
 */
class SinglePixelActivity : AppCompatActivity(){
    companion object {
        val TAG = "SinglePixelActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate--->启动1像素保活")
        window.setGravity(Gravity.START or Gravity.TOP)
        window.attributes.x = 0
        window.attributes.y = 0
        window.attributes.width = 300
        window.attributes.height = 300
        // 绑定SinglePixelActivity到ScreenManager
        ScreenManager.getInstance(WeakReference(this)).setSingleActivity(this)
    }

    /**
     * 如果1像素界面被销毁, 检查SportActivity是否销毁, 如果销毁, 重启
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy--->1像素保活被终止")
        if (!SystemUtils.isAppAlive(this, Contants.PACKAGE_NAME)) {
            val intent = Intent(this, SportActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Log.i(TAG, "SinglePixelActivity---->APP被干掉了，我要重启它")
        }
        super.onDestroy()
    }
}