package cc.aoeiuv020.panovel.ad

import android.app.Application
import android.os.Build
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.settings.AdSettings
import com.qq.e.comm.managers.GDTADManager
import com.qq.e.comm.managers.setting.GlobalSetting

/**
 * Created by AoEiuV020 on 2021.04.25-23:45:31.
 */
object AdHelper {
    fun init(context: Application) {

        if (AdSettings.middle13lmEnabled) {
            // 通过调用此方法初始化 SDK。如果需要在多个进程拉取广告，每个进程都需要初始化 SDK。
            GDTADManager.getInstance().initWith(context, AdConstants.GDT_APP_ID)
            GlobalSetting.setChannel(999)
            GlobalSetting.setEnableMediationTool(true)
        }
    }

    fun createListHelper() = if (BuildConfig.DEBUG && Build.FINGERPRINT.contains("generic")) {
        TestAdListHelper()
    } else {
        GdtAdListHelper()
    }
}