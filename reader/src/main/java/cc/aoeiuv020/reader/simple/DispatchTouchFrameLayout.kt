package cc.aoeiuv020.reader.simple

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2017.11.24-19:54:53.
 */
internal class DispatchTouchFrameLayout : FrameLayout, AnkoLogger {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    var reader: SimpleReader? = null

    private val startPoint = PointF()
    private val sold = ViewConfiguration.get(context).scaledTouchSlop
    private var isClick = false
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
                startPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> if (Math.abs(event.x - startPoint.x) > sold || Math.abs(event.y - startPoint.y) > sold) {
                isClick = false
            }
            MotionEvent.ACTION_UP -> if (isClick) click()
        }
        return super.dispatchTouchEvent(event)
    }

    private fun click() {
        reader?.menuListener?.toggle()
    }
}