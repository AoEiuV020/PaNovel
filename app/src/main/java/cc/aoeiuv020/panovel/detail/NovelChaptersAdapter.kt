package cc.aoeiuv020.panovel.detail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import kotlinx.android.synthetic.main.novel_chapter_item.view.*
import org.jetbrains.anko.AnkoLogger

class NovelChaptersAdapter(private val ctx: Context, val callback: (Int) -> Unit) : RecyclerView.Adapter<NovelChaptersAdapter.Holder>() {
    private var issuesDesc = emptyList<NovelChapter>()
    override fun getItemCount() = issuesDesc.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val issue = issuesDesc[position]
        holder.root.apply {
            name.text = issue.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = Holder(LayoutInflater.from(ctx).inflate(R.layout.novel_chapter_item, parent, false))

    inner class Holder(val root: View) : RecyclerView.ViewHolder(root), AnkoLogger {
        init {
            root.setOnClickListener {
                // 传出话数的顺序索引，
                callback(issuesDesc.size - 1 - layoutPosition)
            }
        }
    }

    fun setChapters(chapters: List<NovelChapter>) {
        issuesDesc = chapters.asReversed()
        notifyDataSetChanged()
    }
}