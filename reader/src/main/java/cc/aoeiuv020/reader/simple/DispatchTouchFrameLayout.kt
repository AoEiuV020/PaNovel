package cc.aoeiuv020.reader.simple

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose

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

    private var previousAction: Int = MotionEvent.ACTION_UP
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        verbose { event }
        if (previousAction == MotionEvent.ACTION_DOWN
                && event.action == MotionEvent.ACTION_UP) {
            reader?.menuListener?.toggle()
        }
        previousAction = event.action
        return super.dispatchTouchEvent(event)
    }
}