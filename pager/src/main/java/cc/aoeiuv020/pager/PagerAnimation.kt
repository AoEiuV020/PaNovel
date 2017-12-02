package cc.aoeiuv020.pager

import android.graphics.Canvas
import android.view.MotionEvent

/**
 *
 * Created by AoEiuV020 on 2017.12.03-03:05:20.
 */
interface PagerAnimation {
    fun draw(canvas: Canvas)
    fun scrollAnim()
    fun onTouchEvent(event: MotionEvent): Boolean
}