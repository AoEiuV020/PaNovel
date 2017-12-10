package cc.aoeiuv020.pager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import cc.aoeiuv020.pager.animation.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.verbose

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
class Pager : View, PageAnimation.OnPageChangeListener, AnkoLogger {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private var mAnim: PagerAnimation? = null
    private val drawListener: PageAnimation.OnPageChangeListener = this
    var actionListener: ActionListener? = null
    private var direction = PagerDirection.NONE
    private var centerRect = Rect(0, 0, 0, 0)
    /**
     * 背景色，同时设置成仿真翻页的背面主色，
     */
    var bgColor: Int = 0xffffffff.toInt()
        set(value) {
            field = value
            (mAnim as? SimulationPageAnim)?.setMainColor(value)
        }
    var drawer: IPagerDrawer = BlankPagerDrawer()
        set(value) {
            field = value
            resetDrawer()
        }
    var margins: Margins = Margins()
        set(value) {
            field = value
            resetAnim()
            resetDrawer()
        }
    var animMode: AnimMode = AnimMode.SIMULATION
        set(value) {
            field = value
            resetAnim()
        }
    var animDurationMultiply: Float = 0.8f
        set(value) {
            field = value
            mAnim?.setDurationMultiply(value)
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        resetDrawer()
        resetAnim(w, h)
        refresh()
        centerRect = Rect(w / 4 * 1,
                h / 4 * 1,
                w / 4 * 3,
                h / 4 * 3
        )
    }

    override fun drawCurrent(backgroundCanvas: Canvas, nextCanvas: Canvas) {
        backgroundCanvas.drawColor(bgColor)
        drawer.drawCurrentPage(backgroundCanvas, nextCanvas)
    }

    override fun hasPrev(): Boolean {
        debug { "prev" }
        direction = PagerDirection.PREV
        actionListener?.onPagePrev()
        return drawer.scrollToPrev()
    }

    override fun hasNext(): Boolean {
        debug { "next" }
        direction = PagerDirection.NEXT
        actionListener?.onPageNext()
        return drawer.scrollToNext()
    }

    override fun pageCancel() {
        debug { "cancel" }
        when (direction) {
            PagerDirection.NEXT -> drawer.scrollToPrev()
            PagerDirection.PREV -> drawer.scrollToNext()
            PagerDirection.NONE -> {
            }
        }
    }

    fun refresh() {
        mAnim?.refresh()
                ?: debug { "anim == null" }
    }

    /**
     * 如果视图没初始化就不重置动画，
     */
    private fun resetAnim() {
        if (width != 0 && height != 0) {
            resetAnim(width, height)
        }
    }

    private fun resetDrawer() {
        if (width != 0 && height != 0) {
            drawer.attach(this,
                    Size(width, height),
                    Size(width - (margins.left + margins.right) * width / 100,
                            height - (margins.top + margins.bottom) * height / 100)
            )
        }
    }

    private fun resetAnim(w: Int, h: Int) {
        val config = AnimationConfig(w, h, margins, this, drawListener, animDurationMultiply)
        mAnim = when (animMode) {
            AnimMode.SIMULATION -> SimulationPageAnim(config).apply { setMainColor(bgColor) }
            AnimMode.COVER -> CoverPageAnim(config)
            AnimMode.SLIDE -> SlidePageAnim(config)
            AnimMode.NONE -> NonePageAnim(config)
            AnimMode.SCROLL -> ScrollPageAnim(config)
        }
    }

    override fun onDraw(canvas: Canvas) {
        verbose { "onDraw" }
        mAnim?.draw(canvas)
    }

    override fun computeScroll() {
        mAnim?.scrollAnim()
    }

    private val startPoint = PointF()
    private val sold = ViewConfiguration.get(context).scaledTouchSlop
    private var isClick = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (centerRect.contains(event.x.toInt(), event.y.toInt())) {
                    isClick = true
                    startPoint.set(event.x, event.y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isClick && (Math.abs(event.x - startPoint.x) > sold || Math.abs(event.y - startPoint.y) > sold)) {
                    isClick = false
                }
                if (isClick) {
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isClick) {
                    click()
                    isClick = false
                    return true
                }
            }
        }
        return mAnim?.onTouchEvent(event) ?: false
    }

    private fun click() {
        actionListener?.onCenterClick()
    }

    override fun callOnClick(): Boolean {
        return super.callOnClick()
    }

    interface ActionListener {
        fun onCenterClick()
        fun onPagePrev()
        fun onPageNext()
    }
}