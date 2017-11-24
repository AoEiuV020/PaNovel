package cc.aoeiuv020.panovel.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.toast


/**
 *
 * Created by AoEiuV020 on 2017.10.15-19:42:24.
 */

abstract class BasePreferenceFragment(private val prefId: Int) : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(prefId)
        setHasOptionsMenu(true)
    }
}

class GeneralPreferenceFragment : BasePreferenceFragment(R.xml.pref_general), AnkoLogger {
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var adEnabledPreference: SwitchPreference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adEnabledPreference = findPreference("ad_enabled") as SwitchPreference
        adEnabledPreference.apply {
            // 如果广告是启用状态，先禁用开关，等激励广告加载，
            isEnabled = !isChecked
            setOnPreferenceChangeListener { preference, newValue ->
                debug { "listen ${preference.key} = $newValue" }
                if (newValue == false) {
                    if (mRewardedVideoAd.isLoaded) {
                        mRewardedVideoAd.show()
                    } else {
                        loadRewardedVideoAd()
                        activity.toast("激励广告没有成功加载")
                    }
                }
                newValue == true
            }
        }

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoAdLeftApplication() {
            }

            override fun onRewardedVideoAdLoaded() {
                adEnabledPreference.isEnabled = true
            }

            override fun onRewardedVideoAdOpened() {
            }

            override fun onRewarded(p0: RewardItem?) {
                adEnabledPreference.isChecked = false
            }

            override fun onRewardedVideoStarted() {
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                if (adEnabledPreference.isChecked) {
                    activity.toast("激励广告加载失败")
                }
            }

        }
        loadRewardedVideoAd()
    }

    private fun loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.loadAd("ca-app-pub-3036112914192534/6364302499", App.adRequest)
        }
    }


    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(activity)
    }

    override fun onResume() {
        super.onResume()
        mRewardedVideoAd.resume(activity)
    }

    override fun onDestroy() {
        mRewardedVideoAd.destroy(activity)
        super.onDestroy()
    }

}

class ReadPreferenceFragment : BasePreferenceFragment(R.xml.pref_read)

class BookListPreferenceFragment : BasePreferenceFragment(R.xml.pref_book_list)
