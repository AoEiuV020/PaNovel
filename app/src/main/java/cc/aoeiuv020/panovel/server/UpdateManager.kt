package cc.aoeiuv020.panovel.server

import android.content.Context
import android.support.annotation.WorkerThread
import android.util.Log
import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.jsonPath
import cc.aoeiuv020.base.jar.notZero
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import cc.aoeiuv020.panovel.settings.ServerSettings
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.VersionUtil
import cc.aoeiuv020.panovel.util.notify
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2018.04.06-02:37:52.
 */
object UpdateManager : AnkoLogger {
    private var novelService: NovelService? = null
    private var outOfVersion: Boolean = false

    fun downloadUpdate(ctx: Context, extra: String) {
        debug { "downloadUpdate $extra" }
        ctx.doAsync({ e ->
            val message = "更新通知解析失败,"
            Reporter.post(message, e)
            error(message, e)
        }) {
            val remoteNovel: Novel = extra.jsonPath.get<String>("novel").toBean()
            requireNotNull(remoteNovel.site)
            requireNotNull(remoteNovel.author)
            requireNotNull(remoteNovel.name)
            requireNotNull(remoteNovel.detail)
            requireNotNull(remoteNovel.chaptersCount)
            val (localNovel, hasUpdate) = DataManager.receiveUpdate(remoteNovel)
            if (!hasUpdate || !ServerSettings.notifyNovelUpdate) {
                // 没有更新或者不通知更新就不继续，
                return@doAsync
            }
            debug {
                "notifyPinnedOnly: ${ServerSettings.notifyPinnedOnly}"
            }
            debug {
                "pinnedTime: ${localNovel.pinnedTime}"
            }
            debug {
                "pinnedTime.notZero: ${localNovel.pinnedTime.notZero()}"
            }
            if (ServerSettings.notifyPinnedOnly && localNovel.pinnedTime.notZero() == null) {
                return@doAsync
            }
            debug {
                "notify update: $localNovel"
            }
            if (ServerSettings.singleNotification) {
                val bitText = DataManager.hasUpdateNovelList()
                        .joinToString("\n") {
                            it.run { "$name: $lastChapterName" }
                        }
                uiThread {
                    it.notify(id = 2,
                            text = localNovel.lastChapterName,
                            title = it.getString(R.string.notify_has_update_title_placeholder, localNovel.name),
                            bigText = bitText,
                            time = localNovel.updateTime.notZero()?.time)
                }
            } else {
                uiThread {
                    it.notify(id = localNovel.nId.toInt(),
                            text = localNovel.lastChapterName,
                            title = it.getString(R.string.notify_has_update_title_placeholder, localNovel.name),
                            time = localNovel.updateTime.notZero()?.time)
                }
            }
        }
    }

    fun query(novel: Novel): Novel? {
        debug { "query ：<${novel.run { "$site.$author.$name" }}>" }
        return try {
            val service = getService() ?: return null
            service.queryList(listOf(novel)).first().also {
                debug { "查询小说<${novel.run { "$site.$author.$name" }}>更新返回: $it" }
            }
        } catch (e: Exception) {
            val message = "查询小说<${novel.run { "$site.$author.$name" }}>失败，"
            error(message, e)
            Reporter.post(message, e)
            null
        }

    }

    fun touch(novel: Novel) {
        debug { "touch ：<${novel.run { "$site.$author.$name" }}>" }
        val service = getService() ?: return
        val result = service.touch(novel)
        debug { "上传<${novel.run { "$site.$author.$name" }}>更新返回: $result" }
    }

    @Synchronized
    @WorkerThread
    private fun getService(): NovelService? {
        debug { "getService <$novelService, $outOfVersion>" }
        // 已经创建就直接返回，
        novelService?.let { return it }
        // 如果版本过低，直接返回空，不继续，
        if (outOfVersion) return null

        val service = if (BuildConfig.DEBUG && Log.isLoggable(loggerTag, Log.DEBUG)) {
            info { "debug mode," }
            NovelServiceImpl(ServerAddress.getAndroidTest())
        } else {
            NovelServiceImpl(ServerAddress.getDefault())
        }
        val currentVersion = VersionName(VersionUtil.getAppVersionName(App.ctx))
        val minVersion = VersionName(service.minVersion())
        info { "getService minVersion $minVersion/$currentVersion" }
        return if (currentVersion < minVersion) {
            outOfVersion = true
            null
        } else {
            novelService = service
            service
        }
    }
}