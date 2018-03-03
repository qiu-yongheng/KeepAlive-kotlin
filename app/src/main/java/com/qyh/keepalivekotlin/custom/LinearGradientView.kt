package com.qyh.keepalivekotlin.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.qyh.keepalivekotlin.R

/**
 * @author 邱永恒
 * @time 2018/1/7  22:42
 * @desc ${TODD}
 */


class LinearGradientView : View {
    private lateinit var paint: Paint
    private var text: String = "这是屌炸天的闪动文字特效"
    private var textColor: Int = Color.BLACK
    private var lightColor: Int = Color.WHITE
    private var textWidth: Int = 0
    private var textHeight: Int = 0
    private var textSize: Float = 70f
    private var textStrokeWidth: Float = 20f
    private var duration: Int = 3000
    private var dx: Int = 0
    private lateinit var rect: Rect
    private var w: Float = 0f
    private var h: Float = 0f

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttr(attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttr(attrs)
        init()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.textSize = textSize
        paint.strokeWidth = textStrokeWidth

        // 测量文字大小
        rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        textWidth = rect.width()
        textHeight = rect.height()


    }

    private fun initAttr(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.LinearGradientView)
        textColor = array.getColor(R.styleable.LinearGradientView_textColor, Color.BLACK)
        lightColor = array.getColor(R.styleable.LinearGradientView_lightColor, Color.WHITE)
        textSize = array.getDimension(R.styleable.LinearGradientView_textSize, 40f)
        textStrokeWidth = array.getDimension(R.styleable.LinearGradientView_textStrokeWidth, 20f)
        text = array.getString(R.styleable.LinearGradientView_text)
        duration = array.getInt(R.styleable.LinearGradientView_duration, 3000)
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        w = ((measuredWidth - textWidth) / 2).toFloat()
        h = ((measuredHeight + textHeight) / 2).toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 创建LinearGradient
        val linearGradient = LinearGradient((w-textWidth + dx), h - textHeight, (w + dx),h - textHeight , intArrayOf(textColor, lightColor, textColor), floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
        paint.shader = linearGradient

        // 绘制文字
        canvas.drawText(text, w, h, paint)
    }

    fun startAnim() {
        val animator = ValueAnimator.ofInt(textWidth * 2)
        animator.addUpdateListener { animation ->
            dx = animation.animatedValue as Int
            invalidate()
        }
        animator.repeatCount = ValueAnimator.INFINITE
        animator.duration = duration.toLong()
        animator.start()
    }
}
