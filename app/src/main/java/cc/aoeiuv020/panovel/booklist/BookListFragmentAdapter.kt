package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import kotlinx.android.synthetic.main.book_list_item.view.*
import org.jetbrains.anko.selector

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:33:36.
 */
class BookListFragmentAdapter(context: Context, val presenter: BookListFragmentPresenter)
    : RecyclerAdapter<BookListData>(context) {
    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<BookListData>
            = ViewHolder(parent, R.layout.book_list_item)

    fun remove(bookList: BookListData, position: Int) {
        BookList.remove(bookList)
        remove(position)
    }

    fun shareBookList(bookList: BookListData) {
        presenter.shareBookList(bookList)
    }

    inner class ViewHolder(parent: ViewGroup?, layoutId: Int) : BaseViewHolder<BookListData>(parent, layoutId) {
        private val name = itemView.ivName
        private val count = itemView.ivCount

        override fun setData(data: BookListData) {
            itemView.setOnClickListener {
                BookListActivity.start(context, data.name)
            }
            itemView.setOnLongClickListener {
                val list = listOf(R.string.remove to { i: Int -> remove(data, i) },
                        R.string.share to { _: Int -> shareBookList(data) })
                context.selector(context.getString(R.string.action), list.unzip().first.map { context.getString(it) }) { _, i ->
                    list[i].second.invoke(layoutPosition)
                }

                true
            }
            name.text = data.name
            count.text = data.list.size.toString()
        }
    }
}