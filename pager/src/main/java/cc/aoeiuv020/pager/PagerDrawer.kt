package cc.aoeiuv020.pager

/**
 *
 * Created by AoEiuV020 on 2017.12.07-23:54:07.
 */
abstract class PagerDrawer : IPagerDrawer {
    var pager: Pager? = null
    protected lateinit var backgroundSize: Size
    protected lateinit var contentSize: Size

    override fun attach(pager: Pager, backgroundSize: Size, contentSize: Size) {
        this.pager = pager
        this.backgroundSize = backgroundSize
        this.contentSize = contentSize
    }
}