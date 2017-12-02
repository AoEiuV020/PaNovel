package cc.aoeiuv020.pager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import cc.aoeiuv020.pager.animation.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
class Pager(context: Context) : View(context), PageAnimation.OnPageChangeListener, AnkoLogger {
    private lateinit var mAnim: PageAnimation
    private var drawer: PagerDrawer? = null
    private var direction = PagerDirection.NONE
    private val listener = this
    private var animMode: AnimMode = AnimMode.SIMULATION

    /**
     * 初始化，在view布局加载完成前调用，
     */
    fun init(drawer: PagerDrawer, animMode: AnimMode = AnimMode.SIMULATION) {
        this.drawer = drawer
        drawer.pager = this
        this.animMode = animMode
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        resetAnim(w, h)
    }

    override fun drawCurrent(backgroundCanvas: Canvas, nextCanvas: Canvas) {
        drawer?.drawFirstPage(backgroundCanvas, nextCanvas)
    }

    override fun drawPrev(backgroundCanvas: Canvas, nextCanvas: Canvas): Boolean {
        debug { "prev" }
        direction = PagerDirection.PREV
        return drawer?.drawPrevPage(backgroundCanvas, nextCanvas) ?: false
    }

    override fun drawNext(backgroundCanvas: Canvas, nextCanvas: Canvas): Boolean {
        debug { "next" }
        direction = PagerDirection.NEXT
        return drawer?.drawNextPage(backgroundCanvas, nextCanvas) ?: false
    }

    override fun pageCancel() {
        debug { "cancel" }
        drawer?.cancel(direction)
    }

    private fun resetAnim(w: Int, h: Int) {
        mAnim = when (animMode) {
            AnimMode.SIMULATION -> SimulationPageAnim(w, h, this, listener)
            AnimMode.COVER -> CoverPageAnim(w, h, this, listener)
            AnimMode.SLIDE -> SlidePageAnim(w, h, this, listener)
            AnimMode.NONE -> NonePageAnim(w, h, this, listener)
            AnimMode.SCROLL -> ScrollPageAnim(w, h, 0, 0, this, listener)
        }
    }

    override fun onDraw(canvas: Canvas) {
        mAnim.draw(canvas)
    }

    override fun computeScroll() {
        mAnim.scrollAnim()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val mPageAnim = mAnim
        mPageAnim.onTouchEvent(event)
        return true
    }
}