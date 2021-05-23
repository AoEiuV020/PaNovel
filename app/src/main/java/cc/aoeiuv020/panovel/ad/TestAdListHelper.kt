package cc.aoeiuv020.panovel.ad

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import com.qq.e.comm.managers.GDTADManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by AoEiuV020 on 2021.04.26-02:23:16.
 */
class TestAdListHelper :
    AdListHelper<String, TestAdListHelper.TestAdItem, TestAdListHelper.TestAdViewHolder>() {
    override val nativeAdEnabled: Boolean
        get() = super.nativeAdEnabled && GDTADManager.getInstance().isInitialized

    private val adService: ExecutorService by lazy {
        Executors.newCachedThreadPool()
    }

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm:ss.SSS")

    override fun realRequestAd(requestAdCount: Int) {
        doAsync({ t ->
            error("请求广告异常", t)
        }, adService) {
            val data = List(requestAdCount) {
                Thread.sleep((200..600).random().toLong())
                sdf.format(Date())
            }
            uiThread {
                onRequestAdResult(data)
            }
        }
    }

    private fun createAdView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_test_ad, parent, false)
    }

    override fun createAdViewHolder(parent: ViewGroup): TestAdViewHolder {
        return TestAdViewHolder(createAdView(parent))
    }

    override fun createItem(): TestAdItem {
        return TestAdItem()
    }

    override fun onAdDestroy() {
        adList.forEach {
            it.isClosed = true
            it.text = null
        }
    }

    class TestAdItem : AdItem<String>() {
        var text: String? = null
        override fun isAdInit(): Boolean {
            return text != null
        }

        override fun bind(ad: String) {
            text = ad
        }
    }

    class TestAdViewHolder(itemView: View) : AdViewHolder<TestAdItem>(itemView) {
        private val rlContainer: FrameLayout = itemView.find(R.id.rlContainer)
        private val tvText: TextView = itemView.find(R.id.tvText)

        override fun bind(item: TestAdItem) {
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