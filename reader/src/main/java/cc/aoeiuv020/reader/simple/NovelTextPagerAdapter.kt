package cc.aoeiuv020.reader.simple

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

internal class NovelTextPagerAdapter(private val simpleReader: SimpleReader) : PagerAdapter(), AnkoLogger {
    private val chapters get() = simpleReader.chapterList
    private val unusedHolders: LinkedList<PageHolder> = LinkedList()
    private val usedHolders: LinkedList<PageHolder> = LinkedList()
    private var current: PageHolder? = null

    override fun isViewFromObject(view: View, obj: Any) = (obj as PageHolder).itemView === view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (unusedHolders.isNotEmpty()) {
            unusedHolders.pop()
        } else {
            PageHolder(simpleReader)
        }.also { usedHolders.push(it) }
        val chapter = chapters[position]
        debug {
            "instantiate $position $chapter"
        }
        container.addView(holder.itemView)
        holder.position = position
        holder.request(position)
        return holder
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        debug { "viewpager current position $position" }
        current = obj as? PageHolder
    }

    fun getCurrentTextCount(): Int? = current?.getTextCount()

    private var textProgress: Int? = null
    fun getCurrentTextProgress(): Int? = current?.getTextProgress() ?: textProgress
    fun setCurrentTextProgress(textProgress: Int) {
        this.textProgress = textProgress
        debug { "setCurrentTextProgress position ${current?.position}" }
        current?.setTextProgress(textProgress)
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        debug {
            "destroy $position"
        }
        val holder = obj as PageHolder
        val view = holder.itemView
        container.removeView(view)
        holder.destroy()
        holder.let {
            usedHolders.remove(it)
            unusedHolders.push(holder)
        }
    }

    override fun getCount() = chapters.size

    fun refreshCurrentChapter() {
        current?.refresh()
    }

    fun notifyAllItemDataSetChanged() {
        (usedHolders + unusedHolders).forEach {
            it.ntrAdapter.notifyDataSetChanged()
        }
    }

    fun notifyAllItemMarginsChanged() {
        (usedHolders + unusedHolders).forEach {
            it.notifyMarginsChanged()
        }
    }

    /**
     * 内容的上下左右间距改变时上面两个都要通知，
     */
    fun notifyAllItemContentSpacingChanged() {
        // 左右，
        notifyAllItemDataSetChanged()
        // 上下，
        notifyAllItemMarginsChanged()
    }

}