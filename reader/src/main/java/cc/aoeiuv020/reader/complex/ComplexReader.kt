package cc.aoeiuv020.reader.complex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.reader.*
import cc.aoeiuv020.reader.complex.page.NetPageLoader
import cc.aoeiuv020.reader.complex.page.PageLoader
import cc.aoeiuv020.reader.complex.page.PageView
import cc.aoeiuv020.reader.complex.utils.ScreenUtils
import cc.aoeiuv020.reader.simple.SimpleConfigChangedListener
import kotlinx.android.synthetic.main.complex.view.*

/**
 *
 * Created by AoEiuV020 on 2017.12.01-20:31:49.
 */
class ComplexReader(override var ctx: Context, novel: Novel, parent: ViewGroup, requester: TextRequester, override var config: ComplexConfig)
    : BaseNovelReader(novel, requester), SimpleConfigChangedListener {
    private val layoutInflater = LayoutInflater.from(ctx)
    private val contentView: View = layoutInflater.inflate(R.layout.complex, parent, true)
    private val pageView: PageView = contentView.read_pv_page
    private val loader = NetPageLoader(this, pageView, requester)
    override var chapterList: List<Chapter>
        get() = super.chapterList
        set(value) {
            super.chapterList = value
            loader.openBook()
        }
    override var currentChapter: Int
        get() = loader.chapterPos
        set(value) {
            loader.skipToChapter(value)
            chapterChangeListener?.onChapterChange()
        }
    override var textProgress: Int
        get() = loader.pagePos
        set(value) {
//        loader.skipToPage(value)
        }
    override var maxTextProgress: Int = 0

    init {
        config.listener = this
        loader.apply {
            setOnPageChangeListener(object : PageLoader.OnPageChangeListener {
                override fun onChapterChange(pos: Int) {
                }

                override fun onLoadChapter(chapters: MutableList<Chapter>?, pos: Int) {
                }

                override fun onCategoryFinish(chapters: MutableList<Chapter>?) {
                }

                override fun onPageCountChange(count: Int) {
                    maxTextProgress = count
                }

                override fun onPageChange(pos: Int) {
                }

            })
        }
        pageView.apply {
            setTouchListener(object : PageView.TouchListener {
                var isHide = true
                override fun center() {
                    isHide = !isHide
                    menuListener?.toggle()
                }

                override fun onTouch(): Boolean {
                    menuListener?.hide()
                    return isHide.also { isHide = true }
                }

                override fun prePage(): Boolean {
                    return true
                }

                override fun nextPage(): Boolean {
                    return true
                }

                override fun cancel() {
                }

            })
        }
    }

    override fun onTextSizeChanged() {
        loader.setTextSize(config.textSize)
    }

    override fun onTextColorChanged() {
        loader.setTextColor(config.textColor)
    }

    override fun onBackgroundColorChanged() {
        loader.setBackgroundColor(config.backgroundColor)
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
        // TODO: 工具类使用全局context太蠢了，
        ScreenUtils.App.setContext(null)
    }
}