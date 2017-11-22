package cc.aoeiuv020.panovel.base.item

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter

/**
 *
 * Created by AoEiuV020 on 2017.11.22-12:02:27.
 */
abstract class BaseItemListAdapter(context: Context, protected val presenter: BaseItemListPresenter<*>)
    : RecyclerAdapter<NovelItem>(context) {

    override fun onViewRecycled(holder: BaseViewHolder<NovelItem>) {
        // header和footer会强转失败，
        (holder as? BaseItemViewHolder<*>)?.destroy()
    }
}

open class DefaultItemListAdapter(context: Context, presenter: BaseItemListPresenter<*>)
    : BaseItemListAdapter(context, presenter) {
    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<NovelItem>
            = DefaultItemViewHolder(presenter, context, parent, R.layout.novel_item)

}
