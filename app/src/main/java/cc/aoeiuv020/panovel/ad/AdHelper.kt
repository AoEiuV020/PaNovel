package cc.aoeiuv020.panovel.ad

import android.app.Application
import android.os.Build
import cc.aoeiuv020.panovel.settings.AdSettings
import org.jetbrains.anko.AnkoLogger

/**
 * Created by AoEiuV020 on 2021.04.25-23:45:31.
 */
object AdHelper : AnkoLogger {
    fun init(context: Application) {
    }

    fun checkSplashAdAvailable() = AdSettings.adEnabled
            && isArm()

    private fun isArm(): Boolean {
        // GDT只支持arm系列，
        // 不能判断SUPPORTED_ABIS，因为模拟器支持arm情况也是优先使用x86导致无法加载广告，
        @Suppress("DEPRECATION")
        return Build.CPU_ABI.contains("arm")
    }

    fun createListHelper() = TestAdListHelper()
}