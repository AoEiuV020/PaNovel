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
    fun refresh()
    fun onTouchEvent(event: MotionEvent): Boolean
    /**
     * 滚动到下一页，不必支持，
     * @return 返回是否成功翻页，
     */
    fun scrollNext(): Boolean

    /**
     * 滚动到下一页，不必支持，
     * 传入滚动动画的起始点，
     */
    fun scrollNext(x: Float, y: Float): Boolean
    fun scrollPrev(): Boolean
    fun scrollPrev(x: Float, y: Float): Boolean
    fun setDurationMultiply(multiply: Float)
}