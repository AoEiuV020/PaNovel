package cc.aoeiuv020.panovel.ad

import android.app.Application
import android.os.Build
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.AdSettings
import cc.aoeiuv020.panovel.util.DnsUtils
import com.qq.e.comm.managers.GDTADManager
import com.qq.e.comm.managers.setting.GlobalSetting
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync

/**
 * Created by AoEiuV020 on 2021.04.25-23:45:31.
 */
object AdHelper : AnkoLogger {
    fun init(context: Application) {

        if (AdSettings.middle13lmEnabled) {
            // 通过调用此方法初始化 SDK。如果需要在多个进程拉取广告，每个进程都需要初始化 SDK。
            GDTADManager.getInstance().initWith(context, AdConstants.GDT_APP_ID)
            GlobalSetting.setChannel(999)
            GlobalSetting.setEnableMediationTool(true)
        }
        check13lm()
    }

    fun checkSplashAdAvailable() = AdSettings.adEnabled
            && GDTADManager.getInstance().isInitialized
            && AdSettings.middle13lmEnabled
            && isArm()

    private fun isArm(): Boolean {
        // GDT只支持arm系列，
        // 不能判断SUPPORTED_ABIS，因为模拟器支持arm情况也是优先使用x86导致无法加载广告，
        @Suppress("DEPRECATION")
        return Build.CPU_ABI.contains("arm")
    }

    private fun check13lm() {
        debug { "check13lm() called" }
        doAsync({ t ->
            Reporter.post("check13lm failed", t)
        }) {
            val enabled: String =
                DnsUtils.parseTxt(AdConstants.HOST_13LM)["enabled"] ?: return@doAsync
            debug { "check13lm() enabled=$enabled" }
            AdSettings.middle13lmEnabled = enabled == "1"
        }
    }

    fun createListHelper() = if (BuildConfig.DEBUG && Build.FINGERPRINT.contains("generic")) {
        TestAdListHelper()
    } else {
        GdtAdListHelper()
    }
}