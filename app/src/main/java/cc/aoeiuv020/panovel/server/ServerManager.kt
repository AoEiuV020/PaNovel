package cc.aoeiuv020.panovel.server

import android.content.Context
import androidx.annotation.WorkerThread
import cc.aoeiuv020.base.jar.notZero
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.server.common.bookId
import cc.aoeiuv020.panovel.server.dal.model.Config
import cc.aoeiuv020.panovel.server.dal.model.Message
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import cc.aoeiuv020.panovel.settings.ServerSettings
import cc.aoeiuv020.panovel.util.*
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2018.04.06-02:37:52.
 */
object ServerManager : AnkoLogger {
    private var novelService: NovelService? = null
    private var outOfVersion: Boolean = false
    private var disabled: Boolean = false
    var config: Config? = null

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
                            time = localNovel.receiveUpdateTime.notZero()?.time,
                            channelId = NotificationChannelId.update)
                }
            } else {
                uiThread {
                    it.notify(id = localNovel.nId.toInt(),
                            text = localNovel.lastChapterName,
                            title = it.getString(R.string.notify_has_update_title_placeholder, localNovel.name),
                            time = localNovel.receiveUpdateTime.notZero()?.time,
                            channelId = NotificationChannelId.update)
                }
            }
        }
    }

    fun queryList(novelMap: Map<Long, Novel>): Map<Long, QueryResponse> {
        debug { "queryList ：${novelMap.map { "${it.key}=${it.value.bookId}" }}" }
        val service = getService() ?: return emptyMap()
        return service.queryList(novelMap).also {
            debug { "查询小说更新返回: $it" }
        }
    }

    fun touch(novel: Novel) {
        // 意义不大，禁用了，
    }

    fun message(): Message? {
        debug { "message ：" }
        return try {
            DnsUtils.txtToBean(ServerAddress.MESSAGE_HOST)
        } catch (e: Exception) {
            warn("get message failed: " + ServerAddress.MESSAGE_HOST, e)
            null
        }
    }

    @Synchronized
    @WorkerThread
    private fun getService(): NovelService? {
        debug { "getService <$novelService, $outOfVersion>" }
        // 已经创建就直接返回，
        novelService?.let { return it }
        // 如果版本过低，直接返回空，不继续，
        if (outOfVersion) return null
        // 暂时禁用了，
        if (disabled) return null

        val config: Config
        try {
            config = DnsUtils.txtToBean(ServerAddress.CONFIG_HOST)
        } catch (e: Exception) {
            warn("get config failed: " + ServerAddress.CONFIG_HOST, e)
            disabled = true
            return null
        }
        this.config = config

        val apiUrl: String = config.apiUrl.takeIf { !it.isNullOrEmpty() } ?: run {
            disabled = true
            return null
        }
        val minVersion = VersionName(config.minVersion)
        val currentVersion = VersionName(VersionUtil.getAppVersionName(App.ctx))
        info { "getService minVersion $minVersion/$currentVersion" }
        if (currentVersion < minVersion) {
            // 如果版本过低，直接返回空，不继续，
            outOfVersion = true
            return null
        }
        val serverAddress =
            ServerAddress.new(ServerSettings.serverAddress.takeIf { !it.isNullOrEmpty() } ?: apiUrl)
        info { "server: " + serverAddress.baseUrl }
        return NovelServiceImpl(serverAddress)
    }
}