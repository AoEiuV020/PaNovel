package cc.aoeiuv020.panovel.migration

import android.content.Context
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.migration.impl.DataMigration
import cc.aoeiuv020.panovel.migration.impl.LoginMigration
import cc.aoeiuv020.panovel.migration.impl.SitesMigration
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.VersionUtil
import org.jetbrains.anko.*
import kotlin.reflect.KClass

/**
 * mvp模式，
 * migration相关的尽量少用外面的拿数据的方法，以免相关方法过时被删除了，
 * 尽量不要用可能被混淆影响的代码，
 *
 * Created by AoEiuV020 on 2018.05.16-22:54:50.
 */
class MigrationPresenter(
        private val ctx: Context
) : Presenter<MigrationView>(), Pref, AnkoLogger {
    // sp file is App.ctx.packageName + "_$name"
    override val name: String = "Migration"
    /**
     * 缓存以前的版本，
     * key is "cachedVersion",
     * Delegate不受混淆影响，
     */
    private var cachedVersion: String by Delegates.string("0")
    /**
     * 获取当前版本，
     */
    private val currentVersion: String = VersionUtil.getAppVersionName(ctx)

    private fun list(vararg list: KClass<out Migration>): List<KClass<out Migration>> = list.toList()

    /**
     * 所有数据迁移封装的Migration,
     * 要保持按版本名排序，
     */
    val list: List<Pair<String, List<KClass<out Migration>>>> = listOf(
            // listOf嵌套时智能识别类型有点问题，这里不用，
            "2.2.2" to list(DataMigration::class, LoginMigration::class)
    )

    fun start() {
        debug { "start," }
        var cachedVersionName = VersionName(cachedVersion)
        val currentVersionName = VersionName(currentVersion)
        when {
            cachedVersionName == currentVersionName -> // 版本没变就直接返回，
                view?.showMigrateComplete(from = currentVersionName, to = currentVersionName)
            cachedVersionName > currentVersionName -> {
                // 降级就记录一下现在的版本，防止反复显示降级，同时下次升级时能正常迁移降级后生成的数据，
                cachedVersion = currentVersion
                view?.showDowngrade(from = cachedVersionName, to = currentVersionName)
                view?.showMigrateComplete(from = cachedVersionName, to = currentVersionName)
            }
            cachedVersionName < currentVersionName -> {
                debug {
                    "migrate start <${cachedVersionName.name} to ${currentVersionName.name}>"
                }
                view?.doAsync({ e ->
                    val message = if (e is MigrateException) {
                        "迁移旧版数据失败，从<${cachedVersionName.name}>到<${e.migration.to.name}>"
                    } else {
                        "未知错误，"
                    }
                    Reporter.post(message, e)
                    error(message, e)
                    ctx.runOnUiThread {
                        if (e is MigrateException) {
                            view?.showMigrateError(from = cachedVersionName, migration = e.migration)
                        } else {
                            view?.showError(message, e)
                        }
                    }
                }) {
                    // 网站列表的迁移单独处理不影响版本号，直接同步最新支持的所有网站到数据库，
                    // 由于操作了数据库，同时会触发room版本迁移，
                    SitesMigration().migrate(ctx, cachedVersionName)
                    // 缓存一开始的版本，迁移完成后展示，
                    val beginVersionName = cachedVersionName
                    list.dropWhile { (versionName, _) ->
                        // 缓存的版本如果大于等于这个Migration的版本，就跳过这个Migration,
                        cachedVersionName >= VersionName(versionName)
                    }.forEach { (versionName, migrations) ->
                        // 遍历所有剩下的Migration分段升级，
                        // ui线程拿到这个cachedVersionName时可能已经变了，所以来个临时变量，虽然无所谓，
                        val from = cachedVersionName
                        migrations.forEach { clazz ->
                            // 反射拿对象，免得加载不需要的migration,
                            val migration = clazz.java.newInstance()
                            uiThread {
                                view?.showUpgrading(from = from, migration = migration)
                            }
                            debug { "migrate $cachedVersionName to ${migration.to}" }
                            try {
                                migration.migrate(ctx, cachedVersionName)
                            } catch (e: Exception) {
                                if (e is MigrateException) {
                                    throw e
                                } else {
                                    throw MigrateException(migration = migration, cause = e)
                                }
                            }

                        }
                        // 每个阶段分别保存升级后的版本，
                        cachedVersionName = VersionName(versionName)
                        cachedVersion = cachedVersionName.name
                    }
                    debug { "migrated $currentVersionName" }
                    // 最后缓存当前版本，
                    cachedVersionName = currentVersionName
                    cachedVersion = cachedVersionName.name
                    uiThread {
                        view?.showMigrateComplete(from = beginVersionName, to = currentVersionName)
                    }
                }
            }
        }
    }

    fun ignoreMigration(migration: Migration) {
        debug { "ignoreMigration $migration" }
        view?.doAsync({ e ->
            val message = "sp保存出错，"
            Reporter.post(message, e)
            error(message, e)
            ctx.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            cachedVersion = migration.to.name
        }
    }
}