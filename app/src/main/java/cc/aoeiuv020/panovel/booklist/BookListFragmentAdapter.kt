package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.BookList
import kotlinx.android.synthetic.main.book_list_item.view.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:33:36.
 */
class BookListFragmentAdapter(
        private val itemListener: ItemListener
) : RecyclerView.Adapter<BookListFragmentAdapter.ViewHolder>() {
    private var _data: MutableList<BookList> = mutableListOf()
    var data: List<BookList>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.book_list_item, parent, false)
        return ViewHolder(itemView, itemListener)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.apply(item)
    }

    class ViewHolder(itemView: View, itemListener: ItemListener) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.ivName
        private val count = itemView.ivCount
        // 提供外面的加调方法使用，
        lateinit var bookList: BookList
            private set
        val ctx: Context = itemView.context

        init {
            itemView.setOnClickListener {
                itemListener.onClick(this)
            }
            itemView.setOnLongClickListener {
                itemListener.onLongClick(this)
            }
        }

        fun apply(bookList: BookList) {
            this.bookList = bookList
            name.text = bookList.name
            // TODO: 改改，顺便要查到数量，
            count.text = ""
        }
    }

    interface ItemListener {
        fun onClick(vh: ViewHolder)
        fun onLongClick(vh: ViewHolder): Boolean
    }
}