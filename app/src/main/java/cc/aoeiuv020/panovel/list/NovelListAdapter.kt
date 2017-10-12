package cc.aoeiuv020.panovel.list

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelListItem
import kotlinx.android.synthetic.main.novel_list_item.view.*
import org.jetbrains.anko.AnkoLogger

class NovelListAdapter(private val ctx: Activity, data: List<NovelListItem>) : BaseAdapter(), AnkoLogger {
    private val items = data.toMutableList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
            = (convertView ?: LayoutInflater.from(ctx).inflate(R.layout.novel_list_item, parent, false)).apply {
        val novel = getItem(position)
        novelName.text = novel.novel.name
        novelAuthor.text = novel.novel.author
        novelInfo.text = novel.info
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size
    fun addAll(novelList: List<NovelListItem>) {
        items.addAll(novelList)
        notifyDataSetChanged()
    }
}