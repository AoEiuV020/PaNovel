package cc.aoeiuv020.panovel.detail

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import kotlinx.android.synthetic.main.novel_chapter_item.view.*
import org.jetbrains.anko.startActivity

class NovelChaptersAdapter(ctx: Context, private val novelItem: NovelItem) : RecyclerAdapter<NovelChapter>(ctx) {
    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<NovelChapter>
            = ViewHolder(parent)

    inner class ViewHolder(parent: ViewGroup?) : BaseViewHolder<NovelChapter>(parent, R.layout.novel_chapter_item) {
        private val nameTextView: TextView = itemView.name
        override fun setData(issue: NovelChapter) {
            super.setData(issue)
            nameTextView.text = issue.name
        }

        override fun onItemViewClick(issue: NovelChapter) {
            // 计算顺序的索引，
            val index = data.size - 1 - layoutPosition
            context.startActivity<NovelTextActivity>("novelItem" to novelItem.toJson(), "index" to index)
        }
    }
}