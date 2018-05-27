package cc.aoeiuv020.panovel.migration

import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.util.VersionName

/**
 * Created by AoEiuV020 on 2018.05.16-22:56:40.
 */
interface MigrationView : IView {
    fun showDowngrade(from: VersionName, to: VersionName)
    // 这个必调用，除非迁移抛异常，其他操作等迁移完了在这个方法内进行，
    fun showMigrateComplete(from: VersionName, to: VersionName)
    fun showUpgrading(from: VersionName, migration: Migration)
    fun showMigrateError(from: VersionName, migration: Migration)
    fun showError(message: String, e: Throwable)
}