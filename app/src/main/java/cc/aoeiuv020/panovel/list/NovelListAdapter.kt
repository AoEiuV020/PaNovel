package cc.aoeiuv020.panovel.list

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.settings.ListSettings
import java.util.*

open class NovelListAdapter(
        private val itemListener: NovelItemActionListener = NovelItemActionAdapter(),
        /**
         * 初始化小说item时调用，用于书架列表隐藏添加书架按钮，
         */
        private val initItem: (View) -> Unit = {}
) : RecyclerView.Adapter<NovelViewHolder>() {
    @Suppress("PropertyName")
    protected open var _data: MutableList<Novel> = mutableListOf()
    var data: List<Novel>
        get() = _data
        set(value) {
            _data = value.toMutableList()
            notifyDataSetChanged()
        }

    // 打开时存个最小时间，手动刷新时更新这个时间，
    // 如果小说刷新时间checkUpdateTime小于这个时间就联网刷新章节列表，
    private var refreshTime = Date(0)

    fun refresh() {
        refreshTime = Date()
    }

    // 这里确保一个adapter只读取一份设置，否则可能改变设置后回来发现新加载的元素是新设置，
    private val dotColor = ListSettings.dotColor
    private val dotSize = ListSettings.dotSize

    // TODO: 要支持多种视图，
    @LayoutRes
    private val layout: Int = R.layout.novel_item_big

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovelViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return NovelViewHolder(itemView, itemListener, initItem, dotColor, dotSize)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: NovelViewHolder, position: Int) {
        val novel = _data[position]
        holder.apply(novel, refreshTime)
    }
}