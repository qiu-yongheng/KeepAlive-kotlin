package com.qyh.keepalivekotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.qyh.keepalivekotlin.SportActivity
import com.qyh.keepalivekotlin.utils.Contants
import com.qyh.keepalivekotlin.utils.SystemUtils

/**
 * @author 邱永恒
 *
 * @time 2018/3/1  13:49
 *
 * @desc
 *  监听系统广播，复活进程
 *  (1) 网络变化广播
 *  (2) 屏幕解锁广播(不能使用静态注册)
 *  (3) 应用安装卸载广播
 *  (4) 开机广播
 */
class KeepAliveReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "KeepAliveReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "AliveBroadcastReceiver---->接收到的系统广播：" + action!!)
        getNetworkBroadcast(context!!, intent)
        if (SystemUtils.isAppAlive(context, Contants.PACKAGE_NAME)) {
            Log.i(TAG, "AliveBroadcastReceiver---->APP还是活着的")
            return
        }
        val intentAlive = Intent(context, SportActivity::class.java)
        intentAlive.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intentAlive)
        Log.i(TAG, "AliveBroadcastReceiver---->复活进程(APP)")
    }

    private fun getNetworkBroadcast(context: Context, intent: Intent) {
        val action = intent.action
        // wifi状态改变
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            when (wifiState) {
                WifiManager.WIFI_STATE_DISABLED -> Toast.makeText(context, "wifi关闭", Toast.LENGTH_SHORT).show()
                WifiManager.WIFI_STATE_ENABLED -> Toast.makeText(context, "wifi开启", Toast.LENGTH_SHORT).show()
                else -> {
                }
            }
        }
        // 连接到一个有效wifi路由器
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == action) {
            val parcelableExtra = intent.getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
            if (null != parcelableExtra) {
                val networkInfo = parcelableExtra as NetworkInfo
                val state = networkInfo.state
                val isConnected = state == NetworkInfo.State.CONNECTED
                if (isConnected) {
                    Toast.makeText(context, "设备连接到一个有效WIFI路由器", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 监听网络连接状态，包括wifi和移动网络数据的打开和关闭
        // 由于上面已经对wifi进行处理，这里只对移动网络进行监听(该方式检测有点慢)
        // 其中，移动网络--->ConnectivityManager.TYPE_MOBILE；
        //       Wifi--->ConnectivityManager.TYPE_WIFI
        //       不明确类型：ConnectivityManager.EXTRA_NETWORK_INFO
        if (ConnectivityManager.CONNECTIVITY_ACTION == action) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (gprs.isConnected) {
                Toast.makeText(context, "移动网络打开", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "移动网络关闭", Toast.LENGTH_SHORT).show()
            }
        }
    }

}