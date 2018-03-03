package com.qyh.keepalivekotlin.app

import android.app.Application
import com.qyh.keepalivekotlin.utils.BroadcastManager

/**
 * @author 邱永恒
 *
 * @time 2018/3/2  17:17
 *
 * @desc ${TODD}
 *
 */
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        BroadcastManager.init(this)
    }
}