package cc.aoeiuv020.pager

import android.graphics.Canvas

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
interface PagerDrawer {

    fun drawCurrentPage(background: Canvas, content: Canvas)

    fun scrollToPrev(): Boolean

    fun scrollToNext(): Boolean
}