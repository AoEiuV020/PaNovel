package cc.aoeiuv020.panovel.list

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.settings.ItemAction
import cc.aoeiuv020.panovel.settings.ListSettings
import java.util.*
import java.util.concurrent.TimeUnit

open class NovelListAdapter(
        /**
         * 初始化小说item时调用，用于书架列表隐藏添加书架按钮，
         */
        private val initItem: (NovelViewHolder) -> Unit = {},
        private val actionDoneListener: (ItemAction, NovelViewHolder) -> Unit = { _, _ -> },
        private val onError: (String, Throwable) -> Unit
) : RecyclerView.Adapter<NovelViewHolder>() {
    @Suppress("PropertyName")
    protected open var _data: MutableList<NovelManager> = mutableListOf()
    var data: List<NovelManager>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }

    // 打开时存个比最小时间大的，自动刷新没刷新过的，也就是刚搜索出来的，
    // 设置一天，以防万一时区问题，
    // 如果小说刷新时间checkUpdateTime小于这个时间就联网刷新章节列表，
    private var refreshTime = Date(TimeUnit.DAYS.toMillis(1))

    fun refresh() {
        refreshTime = Date()
    }

    // 这里确保一个adapter只读取一份设置，否则可能改变设置后回来发现新加载的元素是新设置，
    private val dotColor = ListSettings.dotColor
    private val dotSize = ListSettings.dotSize

    // TODO: 改了视图设置后viewHolder复用会出问题，比如格式视图结果itemView还是列表的，
    @LayoutRes
    private val layout: Int = when {
        ListSettings.gridView && ListSettings.largeView -> R.layout.novel_item_grid_big
        ListSettings.gridView && !ListSettings.largeView -> R.layout.novel_item_grid_small
        !ListSettings.gridView && ListSettings.largeView -> R.layout.novel_item_big
        !ListSettings.gridView && !ListSettings.largeView -> R.layout.novel_item_small
        else -> R.layout.novel_item_big
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovelViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return NovelViewHolder(itemView, dotColor, dotSize, initItem, actionDoneListener, onError)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: NovelViewHolder, position: Int) {
        val novel = _data[position]
        holder.apply(novel, refreshTime)
    }


    fun addAll(list: List<NovelManager>) {
        _data.addAll(list)
        // TODO: 要看看会不要自动滚到底部，不要滚，
        notifyItemRangeInserted(_data.size - list.size, list.size)
    }

    fun clear() {
        _data.clear()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        _data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun move(from: Int, to: Int) {
        if (from == to || from !in _data.indices || to !in _data.indices) {
            // 位置不正确就直接返回，
            return
        }
        // ArrayList直接删除插入的话性能不行，但是无所谓了，
        val novel = _data.removeAt(from)
        _data.add(to, novel)
        notifyItemMoved(from, to)
    }

}