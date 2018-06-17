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
 * 自定义翻页动画视图，
 *
 * 分成背景和前景两个部分，
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
    /**
     * 前景的上下左右留白，
     */
    var margins: IMargins = IMarginsImpl()
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
    var fullScreenClickNextPage: Boolean = false
    /**
     * 单击不翻页的中心大小，单位百分比，
     */
    var centerPercent: Float = 0.5f
        set(value) {
            field = value
            resetCenterRect(width, height)
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        resetDrawer()
        resetAnim(w, h)
        refresh()
        resetCenterRect(w, h)
    }

    private fun resetCenterRect(w: Int, h: Int) {
        val left = (w / 2 * (1 - centerPercent)).toInt()
        val right = w - left
        val top = (h / 2 * (1 - centerPercent)).toInt()
        val bottom = h - top
        centerRect = Rect(left, top, right, bottom)
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
                    Size(width - Math.ceil((margins.left + margins.right) * width / 100.0).toInt(),
                            height - Math.ceil((margins.top + margins.bottom) * height / 100.0).toInt()
                    ))
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
                isClick = true
                startPoint.set(event.x, event.y)
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
                    click(event.x, event.y)
                    isClick = false
                    return true
                }
            }
        }
        return mAnim?.onTouchEvent(event) ?: false
    }

    private fun click(x: Float, y: Float) {
        debug {
            "<$fullScreenClickNextPage, $x, $y>"
        }
        when {
            centerRect.contains(x.toInt(), y.toInt()) -> // 如果点击中心部分，回调退出全屏，
                actionListener?.onCenterClick()
            !fullScreenClickNextPage && x < ((1 - (y / height)) * width) -> // 如果点击对角线左上，翻上页，
                mAnim?.scrollPrev(x, y)
            else -> // 否则翻下页，
                mAnim?.scrollNext(x, y)
        }
    }

    fun scrollNext() = mAnim?.scrollNext() ?: false
    fun scrollPrev() = mAnim?.scrollPrev() ?: false

    interface ActionListener {
        fun onCenterClick()
        /**
         * 无论是否存在上一页都会调用这个方法，
         */
        fun onPagePrev()

        fun onPageNext()
    }
}