package cc.aoeiuv020.panovel.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.toJson
import kotlinx.android.synthetic.main.novel_list_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.12-17:11:53.
 */
class NovelListRecyclerAdapter(private val ctx: Context) : RecyclerView.Adapter<NovelListRecyclerAdapter.ItemViewHolder>(), AnkoLogger {
    companion object {
        private val I_RECYCLER_VIEW_HEADER_COUNT: Int = 2
    }

    private var items: MutableList<NovelListItem> = mutableListOf()
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val novel = items[position]
        holder.apply {
            novelName.text = novel.novel.name
            novelAuthor.text = novel.novel.author
            novelInfo.text = novel.info
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder
            = ItemViewHolder(LayoutInflater.from(ctx).inflate(R.layout.novel_list_item, parent, false)).apply {
        itemView.setOnClickListener {
            val position = layoutPosition - I_RECYCLER_VIEW_HEADER_COUNT
            val novelItem = items[position].novel
            debug { "novel item click $position, $novelItem" }
            ctx.startActivity<NovelDetailActivity>("novelItem" to novelItem.toJson())
        }

    }

    override fun getItemCount(): Int = items.size

    fun setData(novelList: List<NovelListItem>) {
        items = novelList.toMutableList()
        notifyDataSetChanged()
    }

    fun addAll(novelList: List<NovelListItem>) {
        items.addAll(novelList)
        notifyDataSetChanged()
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val novelName: TextView = view.novelName
        val novelAuthor: TextView = view.novelAuthor
        val novelInfo: TextView = view.novelInfo
    }
}