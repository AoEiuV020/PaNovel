package cc.aoeiuv020.panovel.history

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemViewHolder
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter

/**
 *
 * Created by AoEiuV020 on 2017.10.15-18:12:19.
 */
class HistoryAdapter(context: Context, private val historyPresenter: HistoryPresenter) : RecyclerAdapter<NovelItem>(context) {

    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<NovelItem>
            = BaseItemViewHolder(historyPresenter, context, parent, R.layout.novel_item)

    override fun onViewRecycled(holder: BaseViewHolder<NovelItem>) {
        // header和footer会强转失败，
        (holder as? BaseItemViewHolder)?.destroy()
    }
}