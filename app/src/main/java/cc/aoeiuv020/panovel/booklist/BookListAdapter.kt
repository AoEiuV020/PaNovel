package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.local.BookListData
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import kotlinx.android.synthetic.main.book_list_item.view.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:33:36.
 */
class BookListAdapter(context: Context, val presenter: BookListFragmentPresenter)
    : RecyclerAdapter<BookListData>(context) {
    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<BookListData>
            = ViewHolder(parent, R.layout.book_list_item)

    inner class ViewHolder(parent: ViewGroup?, layoutId: Int) : BaseViewHolder<BookListData>(parent, layoutId) {
        private val name = itemView.ivName

        override fun setData(data: BookListData) {
            super.setData(data)
            name.text = data.name
        }

        override fun onItemViewClick(data: BookListData) {
            super.onItemViewClick(data)
            BookListActivity.start(context, data)
        }
    }
}