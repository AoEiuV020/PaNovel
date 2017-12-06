package cc.aoeiuv020.reader.complex

import android.graphics.Canvas
import android.view.View
import cc.aoeiuv020.pager.PagerDrawer

/**
 *
 * Created by AoEiuV020 on 2017.12.03-04:09:17.
 */
class ReaderDrawer(private val backgroundView: View) : PagerDrawer {
    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        backgroundView.apply {
            measure(background.width, background.height)
            layout(0, 0, background.width, background.height)
            draw(background)
        }
    }

    override fun scrollToPrev(): Boolean {
        return true
    }

    override fun scrollToNext(): Boolean {
        return true
    }
}