package cc.aoeiuv020.pager.test

import android.graphics.Canvas
import android.graphics.Paint
import cc.aoeiuv020.pager.PagerDirection
import cc.aoeiuv020.pager.PagerDrawer

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
class NumberPagerDrawer : PagerDrawer() {
    private var n = 0
    private val circlePaint = Paint().apply {
        color = 0xff00ff00.toInt()
    }
    private val textPaint = Paint().apply {
        color = 0xffff0000.toInt()
        textSize = 70f
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.apply {
            drawColor(0xffffffff.toInt())
            drawCircle(width.toFloat() / 2, height.toFloat() / 2, minOf(width, height).toFloat() / 5, circlePaint)
        }
    }

    private fun drawNumber(canvas: Canvas) {
        canvas.apply {
            drawText(n.toString(), width.toFloat() / 2, height.toFloat() / 2, textPaint)
        }
    }

    override fun drawFirstPage(background: Canvas, content: Canvas) {
        drawBackground(background)
        drawNumber(content)
    }

    override fun drawPrevPage(background: Canvas, content: Canvas): Boolean {
        drawBackground(background)
        n--
        drawNumber(content)
        return true
    }

    override fun drawNextPage(background: Canvas, content: Canvas): Boolean {
        drawBackground(background)
        n++
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