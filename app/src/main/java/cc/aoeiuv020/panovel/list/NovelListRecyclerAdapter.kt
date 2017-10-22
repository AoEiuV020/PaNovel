package cc.aoeiuv020.panovel.list

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import kotlinx.android.synthetic.main.novel_list_item.view.*
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2017.10.12-17:11:53.
 */
class NovelListRecyclerAdapter(ctx: Context) : RecyclerAdapter<NovelListItem>(ctx), AnkoLogger {
    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<NovelListItem>
            = ItemViewHolder(parent, R.layout.novel_list_item)

    class ItemViewHolder(parent: ViewGroup, layoutId: Int) : BaseViewHolder<NovelListItem>(parent, layoutId) {
        private val novelName: TextView = itemView.novelName
        private val novelAuthor: TextView = itemView.novelAuthor
        private val novelInfo: TextView = itemView.novelInfo

        override fun setData(novel: NovelListItem) {
            super.setData(novel)
            novelName.text = novel.novel.name
            novelAuthor.text = novel.novel.author
            novelInfo.text = novel.info
        }

        override fun onItemViewClick(novelListItem: NovelListItem) {
            NovelDetailActivity.start(itemView.context, novelListItem.novel)
        }
    }
}