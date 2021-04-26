package cc.aoeiuv020.panovel.ad

import android.app.Application
import com.qq.e.comm.managers.GDTADManager
import com.qq.e.comm.managers.setting.GlobalSetting

/**
 * Created by AoEiuV020 on 2021.04.25-23:45:31.
 */
object AdHelper {
    fun init(context: Application) {

        // 通过调用此方法初始化 SDK。如果需要在多个进程拉取广告，每个进程都需要初始化 SDK。
        GDTADManager.getInstance().initWith(context, "1111762810")
        GlobalSetting.setChannel(999)
        GlobalSetting.setEnableMediationTool(true)
    }

    fun createListHelper() = GdtAdListHelper()
}