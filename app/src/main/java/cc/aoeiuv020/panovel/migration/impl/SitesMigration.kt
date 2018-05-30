package cc.aoeiuv020.panovel.migration.impl

import android.content.Context
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.util.VersionName

/**
 * Created by AoEiuV020 on 2018.05.28-11:31:31.
 */
class SitesMigration : Migration() {
    /**
     * 网站数据直接按最新的处理就好，版本号直接最新，
     * 不包含在其他小版本迁移中，
     */
    override val to: VersionName = VersionName(BuildConfig.VERSION_NAME)
    override val message: String = "刷新支持的网站列表，"

    override fun migrate(ctx: Context, from: VersionName) {
        // 同步所有网站信息到数据库，
        // 如果有网站不再支持，可以在后面加上删除指定条目，
        DataManager.syncSites()
    }
}