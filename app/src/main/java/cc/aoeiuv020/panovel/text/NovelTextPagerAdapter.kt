package cc.aoeiuv020.panovel.text

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.api.NovelChapter
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

class NovelTextPagerAdapter(private val ctx: NovelTextActivity, private val presenter: NovelTextPresenter, private val chaptersAsc: List<NovelChapter>) : PagerAdapter(), AnkoLogger {
    private val unusedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private val usedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private var current: NovelTextViewHolder? = null
    override fun isViewFromObject(view: View, obj: Any) = (obj as NovelTextViewHolder).itemView === view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (unusedHolders.isNotEmpty()) {
            unusedHolders.pop()
        } else {
            NovelTextViewHolder(ctx, presenter)
        }.also { usedHolders.push(it) }
        val chapter = chaptersAsc[position]
        debug {
            "instantiate $position $chapter"
        }
        container.addView(holder.itemView)
        holder.apply(chapter)
        return holder
    }

    override fun setPrimaryItem(container: ViewGroup?, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        current = obj as NovelTextViewHolder
    }

    fun getTextProgress(): Int? {
        return current?.run { itemView.textListView.firstVisiblePosition }
    }

    fun setTextProgress(textProgress: Int) {
        current?.setTextProgress(textProgress)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any?) {
        val holder = obj as NovelTextViewHolder
        val view = holder.itemView
        container.removeView(view)
        holder.let {
            usedHolders.remove(it)
            unusedHolders.push(holder)
        }
    }

    override fun getCount() = chaptersAsc.size

    fun setTextSize(size: Int) {
        debug { "NovelTextPagerAdapter.setTextSize $size" }
        (usedHolders + unusedHolders).forEach {
            it.setTextSize(size)
        }
    }

    fun setLineSpacing(size: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setLineSpacing(size)
        }
    }

    fun setParagraphSpacing(size: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setParagraphSpacing(size)
        }
    }

    fun setTextColor(color: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setTextColor(color)
        }
    }

}