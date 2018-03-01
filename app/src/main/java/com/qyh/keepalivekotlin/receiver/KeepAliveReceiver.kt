package com.qyh.keepalivekotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  13:49
 *
 * @desc
 *  监听系统广播，复活进程
 *  (1) 网络变化广播
 *  (2) 屏幕解锁广播
 *  (3) 应用安装卸载广播
 *  (4) 开机广播
 */
class KeepAliveReceiver : BroadcastReceiver(){
    companion object {
        val TAG = "KeepAliveReceiver"
    }
    override fun onReceive(context: Context?, intent: Intent?) {

    }

}