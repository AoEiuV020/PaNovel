package cc.aoeiuv020.reader.simple

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

internal class NovelTextPagerAdapter(private val simpleReader: SimpleReader) : PagerAdapter(), AnkoLogger {
    private val chapters get() = simpleReader.chapterList
    private val unusedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private val usedHolders: LinkedList<NovelTextViewHolder> = LinkedList()
    private var current: NovelTextViewHolder? = null

    override fun isViewFromObject(view: View, obj: Any) = (obj as NovelTextViewHolder).itemView === view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (unusedHolders.isNotEmpty()) {
            unusedHolders.pop()
        } else {
            NovelTextViewHolder(simpleReader)
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

    override fun setPrimaryItem(container: ViewGroup?, position: Int, obj: Any?) {
        super.setPrimaryItem(container, position, obj)
        debug { "viewpager current position $position" }
        current = obj as? NovelTextViewHolder
    }

    fun getCurrentTextCount(): Int? = current?.getTextCount()

    private var textProgress: Int? = null
    fun getCurrentTextProgress(): Int? = current?.getTextProgress() ?: textProgress
    fun setCurrentTextProgress(textProgress: Int) {
        this.textProgress = textProgress
        debug { "setCurrentTextProgress position ${current?.position}" }
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

}