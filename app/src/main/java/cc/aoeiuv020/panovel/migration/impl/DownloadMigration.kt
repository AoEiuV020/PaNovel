package cc.aoeiuv020.panovel.migration.impl

import android.content.Context
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.settings.DownloadSettings
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.util.VersionName

/**
 * 3.2.4开始添加下载相关专用的设置页，
 * 原先在在GeneralSettings里的下载相关设置迁移到DownloadSettings里，
 *
 * Created by AoEiuV020 on 2018.11.11-12:27:38.
 */
class DownloadMigration : Migration() {
    override val to: VersionName = VersionName("3.2.4")
    override val message: String = "下载设置"

    override fun migrate(ctx: Context, from: VersionName) {
        GeneralSettings.sharedPreferences.all.forEach { (key, value) ->
            when (key) {
                "downloadThreadsLimit" -> DownloadSettings.downloadThreadsLimit = value as Int
                "downloadCount" -> DownloadSettings.downloadCount = value as Int
                "autoDownloadCount" -> DownloadSettings.autoDownloadCount = value as Int
            }
        }
    }
}