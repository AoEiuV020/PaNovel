package cc.aoeiuv020.panovel.server

import android.content.Context
import android.util.Log
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.VersionUtil
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2018.04.06-02:37:52.
 */
object UpdateManager : AnkoLogger {
    var novelService: NovelService? = null

    data class Notification(
            val id: Int,
            val title: String,
            val message: String,
            val time: Long?
    )

    fun downloadUpdate(ctx: Context, extra: String) {
        debug { "downloadUpdate $extra" }
        ctx.doAsync({ e ->
            val message = "更新通知解析失败,"
            Reporter.post(message, e)
            error(message, e)
        }) {
            //            TODO("接收极光更新通知，")
        }
/*
        Observable.create<Notification> { em ->
            val novelJson: String = gson.fromJson(extra, JsonObject::class.java)
                    .getAsJsonPrimitive("novel")
                    .asString
            val novel: Novel = novelJson.toBean()
            // 无视书架上没有的，
            val novelItem = Bookshelf.list().firstOrNull {
                it.requester.extra == novel.requesterExtra
                        && it.requester.type == novel.requesterType
            } ?: run {
                info { "已经不在书架上，${novel.requesterExtra}" }
                em.onComplete()
                return@create
            }
            val cachedChapters = Cache.chapters.get(novelItem)
            // 只对比长度，时间可空真的很麻烦，
            fun Pair<Date?, Int?>.newerThan(other: List<NovelChapter>): Boolean {
                return (second ?: 0 > other.size)
            }
            if (cachedChapters != null
                    && novel.run { updateTime to chaptersCount }.newerThan(cachedChapters)) {
                val novelContext = NovelContext.getNovelContextByUrl(novelItem.requester.url)
                // TODO: 这里考虑加入失败重试，毕竟只有一次机会，服务器知道了更新就不会推第二次，
                val detail = novelContext.getNovelDetail(novelItem.requester)
                val chapters = novelContext.getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
                if (chapters.run { last().update to size }.newerThan(cachedChapters)) {
                    em.onNext(Notification(
                            id = novelItem.hashCode(),
                            title = "《${novelItem.name}》有更新",
                            message = chapters.last().name,
                            time = novel.updateTime?.time
                    ))
                }
            }
            em.onComplete()
        }.async().subscribe({
            context.notify(it.id, it.message, it.title, time = it.time)
        }, { e ->
            error("更新通知解析失败,", e)
        })
*/
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
        novelService ?: return
        doAsync({ e ->
            val message = "上传无更新失败,"
            Reporter.post(message, e)
            error(message, e)
        }) {
            val result = novelService?.touch(novel)
            debug { "上传<${novel.run { "$site.$author.$name" }}>更新返回: $result" }
        }
    }

/*
    fun uploadUpdate(requester: Requester, chaptersCount: Int, updateTime: Date? = null) {
        debug { "uploadUpdate ：${requester.extra}" }
        novelService ?: return
        Observable.fromCallable {
            val novel = Novel().also {
                it.requesterType = requester.type
                it.requesterExtra = requester.extra
                it.chaptersCount = chaptersCount
                it.updateTime = updateTime
            }
            novelService?.uploadUpdate(novel) ?: false
        }.async().subscribe({
            debug { "上传更新返回 $it: ${requester.extra}" }
        }, { e ->
            error("上传更新失败，", e)
        })
    }
*/

    fun create(context: Context) {
        debug { "create ${context.javaClass}" }
        // 如果已经初始化过就直接返回，
        novelService?.let { return }
        // 调试模式直接初始化，
        if (BuildConfig.DEBUG && Log.isLoggable(loggerTag, Log.DEBUG)) {
            debug { "debug mode," }
            novelService = NovelServiceImpl(ServerAddress.getAndroidTest())
            return
        }
        doAsync({ e ->
            val message = "获取服务器信息失败, 尝试默认，"
            Reporter.post(message, e)
            error(message, e)
            novelService = NovelServiceImpl(ServerAddress())
        }) {
            // 从github拿服务器地址，这样可以随时改，至少最低版本需要修改，以达到让用户手中的app过期，不连接服务器，
            val address = ServerAddress.getOnline()
            debug { "ServerAddress ${address.minVersion}: ${address.data}" }
            val currentVersionName = VersionUtil.getAppVersionName(context)
            if (VersionName(address.minVersion) > VersionName(currentVersionName)) {
                // 版本低于要求的，就直接返回，不初始化novelService, 也就拒绝所有服务器请求，
                warn { "minVersion(${address.minVersion}) > currentVersion($currentVersionName)" }
            } else {
                novelService = NovelServiceImpl(address)
            }
        }
    }

    // 回收novelService以便下次重新获取，否则可能这个UpdateManager一直留在内存，唔，真的有必要么，
    // 不用了，
    fun destroy(context: Context) {
        debug { "destroy ${context.javaClass}" }
        novelService = null
    }

}