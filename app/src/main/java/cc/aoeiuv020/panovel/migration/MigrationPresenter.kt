package cc.aoeiuv020.panovel.migration

import android.content.Context
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.migration.impl.LoginMigration
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.PrefDelegates
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.VersionUtil
import org.jetbrains.anko.*

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
    override val name: String = "MigrationPresenter"
    /**
     * 缓存以前的版本，
     * key is "cachedVersion",
     * Delegate不受混淆影响，
     */
    private var cachedVersion: String by PrefDelegates.string("0")
    /**
     * 获取当前版本，
     */
    private val currentVersion: String = VersionUtil.getAppVersionName(ctx)

    /**
     * 所有数据迁移封装的Migration,
     * 要保持按版本名排序，
     */
    private val migrations: List<Migration> = listOf<Migration>(
            LoginMigration()
    ).sortedBy { it.to }

    fun start() {
        debug {
            "start,"
        }
        var cachedVersionName = VersionName(cachedVersion)
        val currentVersionName = VersionName(currentVersion)
        when {
            cachedVersionName == currentVersionName -> // 版本没变就直接返回，
                return
            cachedVersionName > currentVersionName -> {
                // 降级就记录一下现在的版本，防止反复显示降级，同时下次升级时能正常迁移降级后生成的数据，
                cachedVersion = currentVersion
                view?.showDowngrade(from = cachedVersionName, to = currentVersionName)
            }
            cachedVersionName < currentVersionName -> {
                debug {
                    "migrate start <${cachedVersionName.name} to ${currentVersionName.name}>"
                }
                view?.doAsync({ e ->
                    if (e !is MigrateException) {
                        Reporter.unreachable(e)
                        error("不可到达，", e)
                        return@doAsync
                    }
                    val message = "迁移旧版数据失败，从<${cachedVersionName.name}>到<${e.migration.to.name}>"
                    Reporter.post(message, e)
                    error(message, e)
                    ctx.runOnUiThread {
                        view?.showMigrateError(from = cachedVersionName, migration = e.migration)
                    }
                }) {
                    // 缓存一开始的版本，迁移完成后展示，
                    val beginVersionName = cachedVersionName
                    migrations.dropWhile { migration ->
                        // 缓存的版本如果大于等于这个Migration的版本，就跳过这个Migration,
                        cachedVersionName >= migration.to
                    }.forEach { migration ->
                        // 遍历所有剩下的Migration分段升级，
                        // ui线程拿到这个cachedVersionName时可能已经变了，所以来个临时变量，虽然无所谓，
                        val from = cachedVersionName
                        uiThread {
                            view?.showUpgrading(from = from, migration = migration)
                        }
                        try {
                            migration.migrate(ctx, cachedVersionName)
                        } catch (e: Exception) {
                            throw MigrateException(migration = migration, cause = e)
                        }
                        // 每个阶段分别保存升级后的版本，
                        cachedVersionName = migration.to
                        // TODO: 测试一下这个cachedVersion能不能在异步保存，应该没问题，
                        cachedVersion = cachedVersionName.name
                    }
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
        debug {
            "ignoreMigration $migration"
        }
        view?.doAsync({ e ->
            val message = "sp保存出错，"
            Reporter.post(message, e)
            error(message, e)
        }) {
            cachedVersion = migration.to.name
        }
    }
}