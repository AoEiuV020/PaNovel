package cc.aoeiuv020.pager.test

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import cc.aoeiuv020.pager.PagerDirection
import cc.aoeiuv020.pager.PagerDrawer
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
class NumberPagerDrawer : PagerDrawer(), AnkoLogger {
    private var n = 0
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val textPaint = Paint().apply {
        color = 0xffff0000.toInt()
        textSize = 70f
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.apply {
            drawColor(0xffffffff.toInt())
            drawRect(Rect(0, 0, width, height), strokePaint.apply {
                color = 0xff0000ff.toInt()
            })
            drawCircle(width.toFloat() / 2, height.toFloat() / 2, minOf(width, height).toFloat() / 5, strokePaint.apply {
                color = 0xff00ff00.toInt()
            })
        }
    }

    private fun drawNumber(canvas: Canvas) {
        canvas.apply {
            drawRect(Rect(0, 0, width, height), strokePaint.apply {
                color = 0xffff0000.toInt()
            })
            drawText(n.toString(), width.toFloat() / 2, height.toFloat() / 2, textPaint)
        }
    }

    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        debug { "drawCurrentPage $n" }
        drawBackground(background)
        drawNumber(content)
    }

    override fun drawPrevPage(background: Canvas, content: Canvas): Boolean {
        debug { "drawPrevPage $n" }
        if (n < -5) return false
        n--
        drawBackground(background)
        drawNumber(content)
        return true
    }

    override fun drawNextPage(background: Canvas, content: Canvas): Boolean {
        debug { "drawNextPage $n" }
        if (n > 5) return false
        n++
        drawBackground(background)
        drawNumber(content)
        return true
    }

    override fun cancel(direction: PagerDirection) {
        when (direction) {
            PagerDirection.NEXT -> n--
            PagerDirection.PREV -> n++
            PagerDirection.NONE -> {
            }
        }
    }
}