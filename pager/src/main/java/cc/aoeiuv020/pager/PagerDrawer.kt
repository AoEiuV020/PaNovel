package cc.aoeiuv020.pager

import android.graphics.Canvas

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
abstract class PagerDrawer {
    lateinit var pager: Pager

    abstract fun drawCurrentPage(background: Canvas, content: Canvas)

    abstract fun scrollToPrev(): Boolean

    abstract fun scrollToNext(): Boolean
}