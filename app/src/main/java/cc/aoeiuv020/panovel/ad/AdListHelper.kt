package cc.aoeiuv020.panovel.ad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.list.NovelListAdapter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.panovel.util.show
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * Created by AoEiuV020 on 2021.04.26-02:23:16.
 */
class AdListHelper : AnkoLogger {
    companion object {
        const val AD_COUNT = 5
    }

    private val nativeAdEnabled = BuildConfig.DEBUG && GeneralSettings.adEnabled
    private var isDestroy: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private var requesting: Boolean = false
    private val itemsPerAd: Int = when {
        ListSettings.gridView && ListSettings.largeView -> 9
        ListSettings.gridView && !ListSettings.largeView -> 25
        !ListSettings.gridView && ListSettings.largeView -> 6
        !ListSettings.gridView && !ListSettings.largeView -> 8
        else -> 6
    }
    private val adList: MutableList<AdItem> = mutableListOf()
    private var loadedAdCount = 0
    private var showedAdIndex = -1

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

    fun createAdView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
    }

    fun createAdViewHolder(parent: ViewGroup): NovelListAdapter.AdViewHolder {
        return GdtAdViewHolder(createAdView(parent))
    }

    fun adHolderApply(holder: NovelListAdapter.AdViewHolder, position: Int) {
        if (holder !is GdtAdViewHolder) {
            throw IllegalStateException("未知Holder: ${holder.javaClass}")
        }
        val adPosition = getAdPosition(position)
        showedAdIndex = max(showedAdIndex, adPosition)
        if (showedAdIndex >= loadedAdCount - AD_COUNT) {
            // 剩下不到一组广告没有播放就开始继续加载广告了，
            requestAd()
        }
        val item = getAdItem(adPosition)
        holder.apply(item)
    }

    private fun getAdItem(position: Int): AdItem {
        val addCount = position - adList.size + 1
        if (addCount > 0) {
            repeat(addCount) {
                adList.add(AdItem())
            }
            // 广告不够了，获取一波，
            requestAd()
        }
        return adList[position]
    }

    val adService: ExecutorService by lazy {
        Executors.newCachedThreadPool()
    }
    val sdf = SimpleDateFormat("HH:mm:ss.SSS")

    private fun requestAd() {
        debug { "requestAd loadedAdCount=$loadedAdCount" }
        if (!nativeAdEnabled || requesting || isDestroy) {
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
        val startTime = System.currentTimeMillis()
        doAsync({ t ->
            error("请求广告异常", t)
        }, adService) {
            val data = List(requestAdCount) {
                Thread.sleep((200..600).random().toLong())
                sdf.format(Date())
            }
            uiThread {
                debug { "requestAd: cost=${System.currentTimeMillis() - startTime}" }
                // 以防万一adList不够用，
                val addCount = (loadedAdCount + data.size) - adList.size
                if (addCount > 0) {
                    repeat(addCount) {
                        adList.add(AdItem())
                    }
                }
                data.forEach { next ->
                    adList[loadedAdCount++].apply(next)
                }

                requesting = false
                updateShowingAd()
            }
        }
    }

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

    fun getAdId(position: Int): Long {
        return getAdPosition(position).toLong()
    }

    fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
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
        adList.forEach {
            it.isClosed = true
            it.text = null
        }
    }

    class AdItem(
            var text: String? = null,
            var isClosed: Boolean = false
    ) {
        fun isAdInit(): Boolean {
            return !isClosed && text != null
        }

        fun apply(ad: String) {
            text = ad
        }
    }

    class GdtAdViewHolder(itemView: View) : NovelListAdapter.AdViewHolder(itemView) {
        val rlContainer: FrameLayout = itemView.find(R.id.rlContainer)
        val tvText: TextView = itemView.find(R.id.tvText)
        fun apply(item: AdItem) {
            if (item.isClosed || !item.isAdInit()) {
                rlContainer.hide()
            } else {
                rlContainer.show()
            }
            if (item.isAdInit()) {
                tvText.text = item.text
            }
            tvText.setOnClickListener { v ->
                item.isClosed = true
                rlContainer.hide()
            }
        }
    }
}