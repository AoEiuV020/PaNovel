package cc.aoeiuv020.panovel.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.ad.AdHelper
import cc.aoeiuv020.panovel.ad.AdListHelper
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.settings.ItemAction
import cc.aoeiuv020.panovel.settings.ListSettings
import org.jetbrains.anko.AnkoLogger
import java.util.*
import java.util.concurrent.TimeUnit

open class NovelListAdapter(
        /**
         * 初始化小说item时调用，用于书架列表隐藏添加书架按钮，
         */
        private val initItem: (NovelViewHolder) -> Unit = {},
        actionDoneListener: (ItemAction, NovelViewHolder) -> Unit = { _, _ -> },
        private val onError: (String, Throwable) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<NovelListAdapter.BaseViewHolder>(), AnkoLogger {
    init {
        super.setHasStableIds(true)
    }

    private val actualActionDoneListener: (ItemAction, NovelViewHolder) -> Unit = { action, vh ->
        when (action) {
            // CleanData固定删除元素，无视传入的listener,
            ItemAction.CleanData -> remove(vh.layoutPosition)
            else -> actionDoneListener(action, vh)
        }
    }

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
    private val adListHelper: AdListHelper<*, *, *> = AdHelper.createListHelper()

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

    // 保存正在刷新的小说的id，避免重复刷新，以及view复用导致一直显示正在刷新中，
    // 一个列表共用一个，多个列表多个，
    private val refreshingNovelSet = mutableSetOf<Long>()

    // 用于服务器告知有更新时暂存，展示时再刷新，
    private val shouldRefreshSet = mutableSetOf<Long>()

    fun hasUpdate(hasUpdateList: List<Long>) {
        hasUpdateList.forEach {
            shouldRefreshSet.add(it)
        }
        // 刷新列表，开始刷新展示中的viewHolder对应的小说，
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == 1) {
            adListHelper.createAdViewHolder(parent)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            NovelViewHolder(itemView, dotColor, dotSize, refreshingNovelSet, shouldRefreshSet
                    , initItem, actualActionDoneListener, onError)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        adListHelper.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adListHelper.onDestroy()
    }

    override fun getItemViewType(position: Int): Int {
        return if (adListHelper.isAd(position)) {
            1
        } else {
            0
        }
    }

    override fun getItemCount(): Int = adListHelper.getRealSize(data.size)

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is AdListHelper.AdViewHolder<*>) {
            adListHelper.adHolderBind(holder, position)
        } else if (holder is NovelViewHolder) {
            val novel = _data[adListHelper.getItemPosition(position)]
            holder.apply(novel, refreshTime)
        } else {
            throw IllegalStateException("未知holder: ${holder.javaClass}")
        }
    }

    override fun getItemId(position: Int): Long {
        return if (getItemViewType(position) == 1) {
            adListHelper.getAdId(position)
        } else {
            data[adListHelper.getItemPosition(position)].novel.nId
        }
    }


    fun addAll(list: List<NovelManager>) {
        val oldCount = itemCount
        _data.addAll(list)
        // TODO: 要看看会不要自动滚到底部，不要滚，
        notifyItemRangeInserted(oldCount, itemCount - oldCount)
    }

    fun clear() {
        _data.clear()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        _data.removeAt(adListHelper.getItemPosition(position))
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

    abstract class BaseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}