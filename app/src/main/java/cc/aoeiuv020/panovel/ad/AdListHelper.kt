package cc.aoeiuv020.panovel.ad

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.list.NovelListAdapter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.util.notNullOrReport
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import kotlin.math.max
import kotlin.math.min

/**
 * Created by AoEiuV020 on 2021.04.26-02:23:16.
 */
abstract class AdListHelper<AD, IT : AdListHelper.AdItem<AD>, VH : AdListHelper.AdViewHolder<IT>> : AnkoLogger {
    companion object {
        const val AD_COUNT = 5
    }

    open val nativeAdEnabled = BuildConfig.DEBUG && GeneralSettings.adEnabled
    protected val ctx: Context by lazy { recyclerView.context }
    private var isDestroy: Boolean = false
    private var isNoAd: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private var requesting: Boolean = false
    private val itemsPerAd: Int = when {
        ListSettings.gridView && ListSettings.largeView -> 9
        ListSettings.gridView && !ListSettings.largeView -> 25
        !ListSettings.gridView && ListSettings.largeView -> 6
        !ListSettings.gridView && !ListSettings.largeView -> 8
        else -> 6
    }
    protected val adList: MutableList<IT> = mutableListOf()
    private var loadedAdCount = 0
    private var showedAdIndex = -1
    private var requestStartTime: Long = 0

    fun getItemPosition(position: Int): Int {
        return position - position / (itemsPerAd + 1)
    }

    fun getAdPosition(position: Int): Int {
        return position / (itemsPerAd + 1)
    }

    fun getRealSize(size: Int): Int {
        return size + size / itemsPerAd
    }

    fun isAd(position: Int): Boolean {
        return position % (itemsPerAd + 1) == itemsPerAd
    }

    abstract fun createAdViewHolder(parent: ViewGroup): VH

    fun adHolderBind(holder: AdViewHolder<*>, position: Int) {
        val adPosition = getAdPosition(position)
        showedAdIndex = max(showedAdIndex, adPosition)
        if (showedAdIndex >= loadedAdCount - AD_COUNT) {
            // 剩下不到一组广告没有播放就开始继续加载广告了，
            requestAd()
        }
        val item = getAdItem(adPosition)
        @Suppress("UNCHECKED_CAST")
        (holder as VH).bind(item)
    }

    private fun getAdItem(position: Int): IT {
        val addCount = position - adList.size + 1
        if (addCount > 0) {
            repeat(addCount) {
                adList.add(createItem())
            }
            // 广告不够了，获取一波，
            requestAd()
        }
        return adList[position]
    }

    private fun requestAd() {
        debug { "requestAd loadedAdCount=$loadedAdCount" }
        if (!nativeAdEnabled || requesting || isDestroy || isNoAd) {
            return
        }
        if (showedAdIndex < loadedAdCount - AD_COUNT) {
            // 剩下超过一组广告没有播放就先不加载广告了，
            return
        }
        requesting = true
        // 一次请求只有5条，以免太久，
        val requestAdCount = min(getNeedAdCount() - loadedAdCount, AD_COUNT)
        if (requestAdCount <= 0) {
            debug { "requestAd: count=$requestAdCount" }
            requesting = false
            return
        }
        debug { "requestAd: count=$requestAdCount" }
        requestStartTime = System.currentTimeMillis()
        realRequestAd(requestAdCount)
    }

    abstract fun realRequestAd(requestAdCount: Int)

    fun onNoAd() {
        isNoAd = true
    }

    /**
     * 异步加载广告完成后调用这个方法，
     */
    protected fun onRequestAdResult(data: List<AD>) {
        debug { "requestAd: cost=${System.currentTimeMillis() - requestStartTime}" }
        // 以防万一adList不够用，
        val addCount = (loadedAdCount + data.size) - adList.size
        if (addCount > 0) {
            repeat(addCount) {
                adList.add(createItem())
            }
        }
        data.forEach { next ->
            adList[loadedAdCount++].bind(next)
        }

        requesting = false
        updateShowingAd()
    }

    fun onAdClosed(position: Int) {
        getAdItem(getAdPosition(position)).isClosed = true
        recyclerView.adapter?.notifyItemChanged(position)
    }

    abstract fun createItem(): IT

    private fun getNeedAdCount(): Int {
        val realCount = recyclerView.adapter.notNullOrReport().itemCount
        return realCount / (itemsPerAd + 1)
    }

    private fun updateShowingAd() {
        val firstVisibleItemPosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                ?: (recyclerView.layoutManager as? GridLayoutManager)?.findFirstVisibleItemPosition()
                ?: throw IllegalStateException("未知layoutManager")
        val lastVisibleItemPosition = (recyclerView.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                ?: (recyclerView.layoutManager as? GridLayoutManager)?.findLastVisibleItemPosition()
                ?: throw IllegalStateException("未知layoutManager")
        if (firstVisibleItemPosition == RecyclerView.NO_POSITION || lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            debug { "updateAdView 但是没数据" }
            return
        }
        debug { "updateAdView $firstVisibleItemPosition-$lastVisibleItemPosition" }
        (firstVisibleItemPosition..lastVisibleItemPosition).forEach { position ->
            if (isAd(position)) {
                recyclerView.adapter.notNullOrReport().notifyItemChanged(position)
            }
        }
    }

    open fun getAdId(position: Int): Long {
        return getAdPosition(position).toLong()
    }

    @CallSuper
    open fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        debug { "onAttachedToRecyclerView" }
        this.recyclerView = recyclerView
        recyclerView.adapter.notNullOrReport().registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                debug { "onChanged" }
                // 数据量有变化时检查是否需要添加广告，
                requestAd()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                debug { "onItemRangeInserted" }
                // 数据量有变化时检查是否需要添加广告，
                requestAd()
            }
        })
    }

    fun onDestroy() {
        debug { "onDestroy" }
        this.isDestroy = true
        onAdDestroy()
    }

    abstract fun onAdDestroy()

    abstract class AdItem<AD> {
        var isClosed: Boolean = false

        abstract fun isAdInit(): Boolean

        abstract fun bind(ad: AD)
    }

    abstract class AdViewHolder<IT : AdItem<*>>(itemView: View) : NovelListAdapter.BaseViewHolder(itemView) {
        abstract fun bind(item: IT)
    }
}