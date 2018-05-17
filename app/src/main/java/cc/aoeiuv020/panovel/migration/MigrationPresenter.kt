package cc.aoeiuv020.panovel.migration

import android.content.Context
import android.content.SharedPreferences
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.util.*
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
    /**
     * sp file name is ctx.packageName + "_MigrationPresenter"
     */
    override val sharedPreferences: SharedPreferences =
            getSharedPreferences(ctx, "MigrationPresenter")
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
    ).sortedBy { it.to }

    fun start() {
        var cachedVersionName = VersionName(cachedVersion)
        val currentVersionName = VersionName(currentVersion)
        when {
            cachedVersionName == currentVersionName -> // 版本没变就直接返回，
                return
            cachedVersionName > currentVersionName -> {
                // 降级就给个提示，不处理，
                // TODO: 可以判断一下中间有没有需要迁移数据的版本，没有就不用提示了，随便降，
                view?.showDowngrade(from = cachedVersionName, to = currentVersionName)
            }
            cachedVersionName < currentVersionName -> {
                view?.doAsync({ e ->
                    if (e !is MigrateException) {
                        // TODO: 未知错误，需要上报，
                        return@doAsync
                    }
                    val message = "迁移旧版数据失败，从<${cachedVersionName.name}>到<${e.migration.to.name}>"
                    // TODO: 这个异常要上报，按理说不能出异常，但是Migration可能需要跟着升级，改出问题的可能不可避免，
                    error(message, e)
                    ctx.runOnUiThread {
                        view?.showMigrateError(message)
                    }
                }) {
                    migrations.dropWhile { migration ->
                        // 缓存的版本如果大于等于这个Migration的版本，就跳过这个Migration,
                        cachedVersionName >= migration.to
                    }.forEach { migration ->
                        // 遍历所有剩下的Migration分段升级，
                        uiThread {
                            view?.showUpgrading(from = cachedVersionName, to = migration.to)
                        }
                        try {
                            migration.migrate(cachedVersionName)
                        } catch (e: Exception) {
                            throw MigrateException(migration, e)
                        }
                        // 每个阶段分别保存升级后的版本，
                        cachedVersionName = migration.to
                        // TODO: 测试一下这个cachedVersion能不能在异步保存，应该没问题，
                        cachedVersion = cachedVersionName.name
                    }
                    // 最后缓存当前版本，
                    cachedVersionName = currentVersionName
                    cachedVersion = cachedVersionName.name
                }
            }
        }
    }
}