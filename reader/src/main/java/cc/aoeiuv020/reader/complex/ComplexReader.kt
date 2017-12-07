package cc.aoeiuv020.reader.complex

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.reader.BaseNovelReader
import cc.aoeiuv020.reader.Novel
import cc.aoeiuv020.reader.TextRequester
import cc.aoeiuv020.reader.simple.SimpleConfigChangedListener

/**
 *
 * Created by AoEiuV020 on 2017.12.01-20:31:49.
 */
class ComplexReader(override var ctx: Context, novel: Novel, parent: ViewGroup, requester: TextRequester, override var config: ComplexConfig)
    : BaseNovelReader(novel, requester), SimpleConfigChangedListener {
    private val pageView: Pager = Pager(ctx)
    private val drawer = ReaderDrawer(this, novel, requester)
    override var maxTextProgress: Int = 0
    override var currentChapter: Int = 0
    override var textProgress: Int = 0

    init {
        config.listener = this
        pageView.drawer = drawer
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