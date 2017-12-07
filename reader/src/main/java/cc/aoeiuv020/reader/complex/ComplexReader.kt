package cc.aoeiuv020.reader.complex

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.reader.*

/**
 *
 * Created by AoEiuV020 on 2017.12.01-20:31:49.
 */
class ComplexReader(override var ctx: Context, novel: Novel, parent: ViewGroup, requester: TextRequester, override var config: ReaderConfig)
    : BaseNovelReader(novel, requester), ConfigChangedListener {
    private val pageView: Pager = Pager(ctx)
    private val drawer = ReaderDrawer(this, novel, requester)
    override var maxTextProgress: Int = 0
    override var currentChapter: Int
        get() = drawer.chapterIndex
        set(value) {
            drawer.apply {
                chapterIndex = value
                pageIndex = 0
                pager?.refresh()
            }
        }
    override var textProgress: Int = 0

    init {
        config.listener = this
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

    override fun onTextSizeChanged() {
    }

    override fun onTextColorChanged() {
    }

    override fun onBackgroundColorChanged() {
    }

    override fun onBackgroundImageChanged() {
    }

    override fun onLineSpacingChanged() {
    }

    override fun onParagraphSpacingChanged() {
    }

    override fun onLeftSpacingChanged() {
    }

    override fun onTopSpacingChanged() {
    }

    override fun onRightSpacingChanged() {
    }

    override fun onBottomSpacingChanged() {
    }

    override fun refreshCurrentChapter() {
    }

    override fun onDestroy() {
    }
}