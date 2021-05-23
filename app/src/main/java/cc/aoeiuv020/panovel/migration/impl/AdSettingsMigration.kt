package cc.aoeiuv020.panovel.migration.impl

import android.content.Context
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.settings.AdSettings
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.util.VersionName

/**
 * 3.4.1添加专门的广告设置相关，
 * 老的广告开关迁移，
 */
class AdSettingsMigration : Migration() {
    override val to: VersionName = VersionName("3.4.1")
    override val message: String = "广告设置"

    override fun migrate(ctx: Context, from: VersionName) {
        GeneralSettings.sharedPreferences.all.forEach { (key, value) ->
            when (key) {
                "adEnabled" -> AdSettings.adEnabled = value as Boolean
            }
        }
    }
}