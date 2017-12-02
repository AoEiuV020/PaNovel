package cc.aoeiuv020.pager.test

import android.graphics.Canvas
import android.view.View
import cc.aoeiuv020.pager.PagerDrawer

/**
 *
 * Created by AoEiuV020 on 2017.12.03-03:38:49.
 */
class LayoutDrawer(private val view: View) : PagerDrawer {
    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        view.measure(background.width, background.height)
        view.layout(0, 0, background.width, background.height)
        view.draw(background)
    }

    override fun scrollToPrev(): Boolean {
        return true
    }

    override fun scrollToNext(): Boolean {
        return true
    }
}