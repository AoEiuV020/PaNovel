package cc.aoeiuv020.panovel.server

import android.content.Context
import android.util.Log
import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.jsonPath
import cc.aoeiuv020.base.jar.notZero
import cc.aoeiuv020.base.jar.toBean
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
        val service = novelService ?: return null
        return try {
            service.query(novel)
        } catch (e: Exception) {
            val message = "查询小说<${novel.run { "$site.$author.$name" }}>失败，"
            error(message, e)
            Reporter.post(message, e)
            null
        }

    }

    fun touch(novel: Novel) {
        debug { "touch ：<${novel.run { "$site.$author.$name" }}>" }
        val service = novelService ?: return
        doAsync({ e ->
            val message = "上传刷新结果失败,"
            Reporter.post(message, e)
            error(message, e)
        }) {
            val result = service.touch(novel)
            debug { "上传<${novel.run { "$site.$author.$name" }}>更新返回: $result" }
        }
    }

    fun create(context: Context) {
        debug { "create ${context.javaClass}" }
        // 以防万一，如果已经初始化过就直接返回，
        novelService?.let { return }
        // 调试模式直接初始化，
        if (BuildConfig.DEBUG && Log.isLoggable(loggerTag, Log.DEBUG)) {
            debug { "debug mode," }
            novelService = NovelServiceImpl(ServerAddress.getAndroidTest())
            return
        } else {
            // 先给个默认地址，毕竟正常情况就是默认地址使用，
            novelService = NovelServiceImpl(ServerAddress())
        }
        doAsync({ e ->
            // 出错了就继续用默认地址，
            // 如果是断网，就让这连接不断失败，没什么影响，
            val message = "获取服务器信息失败, "
            Reporter.post(message, e)
            error(message, e)
        }) {
            // 从github拿服务器地址，这样可以随时改，至少最低版本需要修改，以达到让用户手中的app过期，不连接服务器，
            val address = ServerAddress.getOnline()
            debug { "ServerAddress ${address.minVersion}: ${address.data}" }
            val currentVersionName = VersionUtil.getAppVersionName(context)
            novelService = if (VersionName(address.minVersion) > VersionName(currentVersionName)) {
                // 版本低于要求的，就直接返回，novelService设为空，拒绝请求，
                warn { "minVersion(${address.minVersion}) > currentVersion($currentVersionName)" }
                null
            } else {
                // 不管地址是否有不同，都覆盖默认配置，没影响，
                NovelServiceImpl(address)
            }
        }
    }

    // 回收novelService以便下次重新获取，否则可能这个UpdateManager一直留在内存，唔，真的有必要么，
    // 不用了，
    @Suppress("unused")
    fun destroy(context: Context) {
        debug { "destroy ${context.javaClass}" }
        novelService = null
    }

}