package com.qyh.eyekotlin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * @author 邱永恒
 *
 * @time 2018/2/16  15:44
 *
 * @desc 函数扩展
 *
 */

/**
 * 显示吐司
 */
fun Context.showToast(message: String) : Toast {
    val toast: Toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    //toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
    return toast
}

/**
 * 界面跳转
 * inline: 内联函数
 * reified: 具体对象
 */
inline fun <reified T: Activity> Activity.newIntent() {
    // T::class.java反射获取class对象
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

