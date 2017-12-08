package cc.aoeiuv020.pager

import android.graphics.Canvas

/**
 *
 * Created by AoEiuV020 on 2017.12.03-03:08:23.
 */
class BlankPagerDrawer : PagerDrawer() {
    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        background.drawColor(0xffffffff.toInt())
    }

    override fun scrollToPrev(): Boolean {
        return true
    }

    override fun scrollToNext(): Boolean {
        return true
    }
}