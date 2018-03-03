package com.qyh.keepalivekotlin.custom

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * @author 邱永恒
 *
 * @time 2018/3/2  11:15
 *
 * @desc ${TODD}
 *
 */
class SwipeBackLayout : FrameLayout {
    private var viewDragHelper: ViewDragHelper? = null
    private var contentView: View? = null
    private var listener: SwipeBackListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        viewDragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {

            /**
             * 只捕获第一个子布局
             */
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return contentView == child
            }

            /**
             * 记录子布局位置变化
             */
            override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
                if (contentView == changedView) {
                    if (contentView!!.left == width) {
                        if (listener != null) {
                            listener!!.onFinish()
                        }
                    }
                    // 设置背景色变化
                    setBackgroundColor(
                            Color.argb(((width - left) / width.toFloat() * 160).toInt(), 0, 0, 0))
                }
            }

            /**
             * 当手指松开之后，处理子布局，如果当前距离大于等于1/3界面宽度，则触发关闭
             * TODO 一定得重写computeScroll()方法，不然没有效果
             */
            override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
                if (contentView == releasedChild) {
                    if (contentView!!.left >= width / 3) {
                        viewDragHelper!!.settleCapturedViewAt(width, top)
                    } else {
                        viewDragHelper!!.settleCapturedViewAt(0, top)
                    }
                    invalidate()
                }
            }

            /**
             * 处理水平拖动
             */
            override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
                return if (contentView == child) {
                    Math.min(Math.max(left, 0), width) // left值在0到屏幕宽度之间
                } else {
                    super.clampViewPositionHorizontal(child, left, dx)
                }
            }

        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 保证只有一个子布局
        if (childCount != 1) {
            throw IllegalStateException("SwipeBackLayout must have only one child.")
        } else {
            contentView = getChildAt(0)
            if (contentView!!.background == null) { // 没有背景色，则设置背景色
                contentView!!.setBackgroundColor(-0x111112)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        viewDragHelper!!.processTouchEvent(ev)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        // TODO 一定要做这个操作，否则onViewReleased不起作用
        if (viewDragHelper!!.continueSettling(true)) {
            invalidate()
        }
    }

    interface SwipeBackListener {
        fun onFinish()
    }

    class SwipeBackFinishActivityListener(private val activity: Activity) : SwipeBackListener {

        override fun onFinish() {
            activity.finish()
            activity.overridePendingTransition(0, 0)
        }

    }

    fun setSwipeBackListener(listener: SwipeBackListener) {
        this.listener = listener
    }

}
