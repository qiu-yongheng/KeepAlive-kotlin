package com.qyh.keepalivekotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.qyh.keepalivekotlin.custom.SwipeBackLayout
import com.qyh.keepalivekotlin.utils.BroadcastManager
import com.qyh.keepalivekotlin.utils.Contants.Companion.TIME_ACTION
import kotlinx.android.synthetic.main.activity_lock_screen.*


/**
 * @author 邱永恒
 *
 * @time 2018/3/2  11:15
 *
 * @desc ${TODD}
 *
 */
class LockScreenActivity : AppCompatActivity() {
    var decorView: View? = null
    var startX: Float = 0f

    companion object {
        val TAG = "LockScreenActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD) // FLAG_DISMISS_KEYGUARD用于去掉系统锁屏页
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) // FLAG_SHOW_WHEN_LOCKED使Activity在锁屏时仍然能够显示

        if (Build.VERSION.SDK_INT >= 21) {
            decorView = window.decorView
            decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }

        setContentView(R.layout.activity_lock_screen)
        Log.d(TAG, "onCreate() --> 启动自定义锁屏")

        swipe_layout.setSwipeBackListener(SwipeBackLayout.SwipeBackFinishActivityListener(this))
        shimmer_text.startAnim()
        BroadcastManager.getInstance().addAction(TIME_ACTION, object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getStringExtra("String")
                tv_time.text = time
            }

        })
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        val action = event?.action
//        val width = decorView?.width
//        val height = decorView?.height
//        val measuredWidth = decorView?.measuredWidth
//        val measuredHeight = decorView?.measuredHeight
//        Log.d(TAG, "width = $width, height = $height, measuredWidth = $measuredWidth, measuredHeight = $measuredHeight")
//        when (action) {
//            MotionEvent.ACTION_DOWN -> {
//                startX = event.x
//            }
//            MotionEvent.ACTION_MOVE -> {
//                decorView?.translationX = event.x - startX
//            }
//            MotionEvent.ACTION_UP -> {
//                if ((event.x - startX) > width?.div(3)!!) {
//                    decorView?.animate()?.translationX(width + 0f)?.setListener(object : Animator.AnimatorListener {
//                        override fun onAnimationRepeat(animation: Animator?) {}
//
//                        override fun onAnimationCancel(animation: Animator?) {}
//
//                        override fun onAnimationStart(animation: Animator?) {}
//
//                        override fun onAnimationEnd(animation: Animator?) {
//                            finish()
//                        }
//                    })?.duration = 100
//
//                } else {
//                    decorView?.animate()?.translationX(0f)?.duration = 100
//                }
//            }
//        }
//        return super.onTouchEvent(event)
//    }

    override fun onBackPressed() {}
}