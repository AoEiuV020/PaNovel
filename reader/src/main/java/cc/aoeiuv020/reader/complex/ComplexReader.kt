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
    val autoRefreshThread: AutoRefreshThread = AutoRefreshThread()
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
        pageView.centerPercent = config.centerPercent
        pageView.animDurationMultiply = config.animationSpeed
        pageView.fullScreenClickNextPage = config.fullScreenClickNextPage
        pageView.bgColor = config.backgroundColor
        pageView.animMode = config.animationMode.toAnimMode()
        pageView.margins = config.contentMargins
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

        autoRefreshThread.start()
    }

    override fun refreshCurrentChapter() {
        drawer.refreshCurrentChapter()
    }

    override fun scrollNext(): Boolean = pageView.scrollNext()
    override fun scrollPrev(): Boolean = pageView.scrollPrev()

    override fun onDestroy() {
        autoRefreshThread.cancel()
        parent.removeView(pageView)
    }

    inner class AutoRefreshThread : Thread() {
        var canceled = false
        var leftTime = config.autoRefreshInterval
        fun reset() {
            leftTime = config.autoRefreshInterval
        }

        fun cancel() {
            canceled = true
            interrupt()
        }

        override fun run() {
            if (leftTime == 0) {
                // 间隔设置为0表示不刷新，直接结束这个方法就好，
                return
            }
            while (!canceled) {
                try {
                    sleep(1000)
                } catch (_: Exception) {
                }
                if (!canceled) {
                    leftTime--
                    if (leftTime == 0) {
                        drawer.pager?.refresh()
                        reset()
                    }
                }
            }
        }
    }
}