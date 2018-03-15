package cc.aoeiuv020.pager

import android.graphics.Canvas

/**
 *
 * Created by AoEiuV020 on 2017.12.02-17:58:54.
 */
interface IPagerDrawer {

    fun attach(pager: Pager, backgroundSize: Size, contentSize: Size)

    fun drawCurrentPage(background: Canvas, content: Canvas)

    fun scrollToPrev(): Boolean

    fun scrollToNext(): Boolean

    fun detach()
}