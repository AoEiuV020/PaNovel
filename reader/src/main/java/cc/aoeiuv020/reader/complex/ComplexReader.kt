package cc.aoeiuv020.reader.complex

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.reader.BaseNovelReader
import cc.aoeiuv020.reader.Novel
import cc.aoeiuv020.reader.ReaderConfig
import cc.aoeiuv020.reader.TextRequester
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2017.12.01-20:31:49.
 */
class ComplexReader(override var ctx: Context, novel: Novel, private val parent: ViewGroup, requester: TextRequester, override var config: ReaderConfig)
    : BaseNovelReader(novel, requester), AnkoLogger {
    private val pageView: Pager = Pager(ctx)
    private val drawer = ReaderDrawer(this, novel, requester)
    override val maxTextProgress: Int
        get() = drawer.pagesCache[currentChapter]?.lastIndex ?: 0
    override var currentChapter: Int
        get() = drawer.chapterIndex
        set(value) {
            drawer.apply {
                chapterIndex = value
                pageIndex = 0
                pager?.refresh()
            }
        }
    override var textProgress: Int
        get() = drawer.pageIndex
        set(value) {
            drawer.apply {
                pageIndex = value
                pager?.refresh()
            }
        }

    init {
        pageView.animDurationMultiply = config.animationSpeed
        pageView.fullScreenClickNextPage = config.fullScreenClickNextPage
        pageView.bgColor = config.backgroundColor
        pageView.animMode = config.animationMode.toAnimMode()
        pageView.margins = config.margins
        pageView.drawer = drawer
        pageView.actionListener = object : Pager.ActionListener {
            override fun onCenterClick() {
                menuListener?.toggle()
            }

            override fun onPagePrev() {
                menuListener?.hide()
            }

            override fun onPageNext() {
                menuListener?.hide()
            }

        }
        parent.addView(pageView)

    }

    override fun refreshCurrentChapter() {
        drawer.refreshCurrentChapter()
    }

    override fun onDestroy() {
        parent.removeView(pageView)
    }
}