package cc.aoeiuv020.panovel.migration

import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.util.VersionName

/**
 * Created by AoEiuV020 on 2018.05.16-22:56:40.
 */
interface MigrationView : IView {
    fun showDowngrade(from: VersionName, to: VersionName)
    fun showMigrateComplete(from: VersionName, to: VersionName)
    fun showUpgrading(from: VersionName, migration: Migration)
    fun showMigrateError(from: VersionName, migration: Migration)
}