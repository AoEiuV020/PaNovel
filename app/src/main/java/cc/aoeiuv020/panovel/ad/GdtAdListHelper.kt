package cc.aoeiuv020.panovel.ad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import com.qq.e.ads.nativ.ADSize
import com.qq.e.ads.nativ.NativeExpressAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.comm.managers.GDTADManager
import com.qq.e.comm.util.AdError
import org.jetbrains.anko.find

/**
 * Created by AoEiuV020 on 2021.04.26-02:23:16.
 */
class GdtAdListHelper : AdListHelper<NativeExpressADView, GdtAdListHelper.GdtAdItem, GdtAdListHelper.GdtAdViewHolder>(), NativeExpressAD.NativeExpressADListener {
    override val nativeAdEnabled: Boolean
        get() = super.nativeAdEnabled && GDTADManager.getInstance().isInitialized
    private val mADManager: NativeExpressAD by lazy {
        NativeExpressAD(ctx, ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT), "9061885053908861", this)
    }

    override fun realRequestAd(requestAdCount: Int) {
        mADManager.loadAD(requestAdCount)
    }

    private fun createAdView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
    }

    override fun createAdViewHolder(parent: ViewGroup): GdtAdViewHolder {
        return GdtAdViewHolder(createAdView(parent))
    }

    override fun createItem(): GdtAdItem {
        return GdtAdItem()
    }

    override fun onAdDestroy() {
        adList.forEach {
            it.isClosed = true
            it.adView?.destroy()
            it.adView = null
        }
    }

    class GdtAdItem : AdItem<NativeExpressADView>() {
        var adView: NativeExpressADView? = null
        override fun isAdInit(): Boolean {
            return adView != null
        }

        override fun bind(ad: NativeExpressADView) {
            adView = ad
        }
    }

    class GdtAdViewHolder(itemView: View) : AdViewHolder<GdtAdItem>(itemView) {
        private val rlContainer: FrameLayout = itemView.find(R.id.rlContainer)

        override fun bind(item: GdtAdItem) {
            if (item.isClosed || !item.isAdInit()) {
                rlContainer.hide()
            } else {
                rlContainer.show()
            }
            rlContainer.removeAllViews()
            if (item.isAdInit()) {
                item.adView?.run {
                    (parent as? ViewGroup)?.removeAllViews()
                    setTag(R.id.ad_view_position, adapterPosition)
                    rlContainer.addView(this)
                    render()
                }
            }
        }
    }

    override fun onADCloseOverlay(p0: NativeExpressADView) {
    }

    override fun onADLoaded(p0: MutableList<NativeExpressADView>) {
        if (isDestroy) {
            p0.forEach {
                it.destroy()
            }
            return
        }
        onRequestAdResult(p0)
    }

    override fun onADOpenOverlay(p0: NativeExpressADView) {
    }

    override fun onRenderFail(p0: NativeExpressADView) {
    }

    override fun onADExposure(p0: NativeExpressADView) {
    }

    override fun onADClosed(p0: NativeExpressADView) {
        val position = p0.getTag(R.id.ad_view_position) as Int
        onAdClosed(position)
    }

    override fun onADLeftApplication(p0: NativeExpressADView) {
    }

    override fun onNoAD(p0: AdError?) {
        Reporter.post("onNoAD, error code: ${p0?.errorCode}, error msg: ${p0?.errorMsg}")
        onNoAd()
        onRequestAdResult(emptyList())
    }

    override fun onADClicked(p0: NativeExpressADView) {
    }

    override fun onRenderSuccess(p0: NativeExpressADView) {
    }
}