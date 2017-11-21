package cc.aoeiuv020.panovel.text

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.api.NovelChapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

class NovelTextPagerAdapter(private val ctx: NovelTextActivity, private val presenter: NovelTextPresenter) : PagerAdapter(), AnkoLogger {
    private var chaptersAsc: List<NovelChapter> = emptyList()
    private val unusedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private val usedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private var current: NovelTextViewHolder? = null

    fun setChaptersAsc(chapters: List<NovelChapter>) {
        this.chaptersAsc = chapters
        notifyDataSetChanged()
    }
    override fun isViewFromObject(view: View, obj: Any) = (obj as NovelTextViewHolder).itemView === view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (unusedHolders.isNotEmpty()) {
            unusedHolders.pop()
        } else {
            NovelTextViewHolder(ctx, presenter.subPresenter())
        }.also { usedHolders.push(it) }
        val chapter = chaptersAsc[position]
        debug {
            "instantiate $position $chapter"
        }
        container.addView(holder.itemView)
        holder.position = position
        holder.apply(chapter)
        return holder
    }

    override fun setPrimaryItem(container: ViewGroup?, position: Int, obj: Any?) {
        super.setPrimaryItem(container, position, obj)
        debug { "viewpager current position $position" }
        current = obj as? NovelTextViewHolder
    }

    fun getTextProgress(): Int? {
        return current?.getTextProgress()
    }

    fun setTextProgress(textProgress: Int) {
        debug { "setTextProgress position ${current?.position}" }
        current?.setTextProgress(textProgress)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any?) {
        debug {
            "destroy $position"
        }
        val holder = obj as NovelTextViewHolder
        val view = holder.itemView
        container.removeView(view)
        holder.destroy()
        holder.let {
            usedHolders.remove(it)
            unusedHolders.push(holder)
        }
    }

    override fun getCount() = chaptersAsc.size

    fun setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
        (usedHolders + unusedHolders).forEach {
            it.setMargins(left, top, right, bottom)
        }
    }

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