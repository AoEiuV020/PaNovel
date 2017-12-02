package cc.aoeiuv020.reader.simple

import android.content.Context
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cc.aoeiuv020.reader.*
import kotlinx.android.synthetic.main.simple.view.*

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:14:20.
 */
internal class SimpleReader(override var ctx: Context, novel: Novel, parent: ViewGroup, requester: TextRequester, override var config: SimpleConfig)
    : BaseNovelReader(novel, requester), SimpleConfigChangedListener {
    private val layoutInflater = LayoutInflater.from(ctx)
    private val contentView: View = layoutInflater.inflate(R.layout.simple, parent, true)
    private val viewPager: ViewPager = contentView.viewPager
    private val background: ImageView = contentView.ivBackground
    private val dtfRoot: DispatchTouchFrameLayout = contentView.dtfRoot
    private val ntpAdapter: NovelTextPagerAdapter
    override var chapterList: List<Chapter>
        get() = super.chapterList
        set(value) {
            super.chapterList = value
            ntpAdapter.notifyDataSetChanged()
        }
    override var textProgress: Int
        get() = ntpAdapter.getCurrentTextProgress() ?: 0
        set(value) {
            ntpAdapter.setCurrentTextProgress(value)
        }
    override val maxTextProgress: Int
        get() = ntpAdapter.getCurrentTextCount() ?: 0
    override var currentChapter: Int
        get() = viewPager.currentItem
        set(value) {
            viewPager.currentItem = value
        }

    init {
        config.listener = this
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                menuListener?.hide()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                currentChapter = position
                chapterChangeListener?.onChapterChange()
            }
        })
        dtfRoot.reader = this
        ntpAdapter = NovelTextPagerAdapter(this)
        viewPager.adapter = ntpAdapter
    }

    override fun onDestroy() {
        // 清空viewPager，自动调用destroyItem切断presenter,
        viewPager.adapter = null
    }

    override fun refreshCurrentChapter() {
        ntpAdapter.refreshCurrentChapter()
    }

    override fun onTextSizeChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onTextColorChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onBackgroundColorChanged() {
        background.setBackgroundColor(config.backgroundColor)
    }

    override fun onBackgroundImageChanged() {
        background.setImageURI(config.backgroundImage)
    }

    override fun onLineSpacingChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onParagraphSpacingChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onRightSpacingChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onLeftSpacingChanged() {
        ntpAdapter.notifyAllItemDataSetChanged()
    }

    override fun onTopSpacingChanged() {
        ntpAdapter.notifyAllItemMarginsChanged()
    }

    override fun onBottomSpacingChanged() {
        ntpAdapter.notifyAllItemMarginsChanged()
    }

}

