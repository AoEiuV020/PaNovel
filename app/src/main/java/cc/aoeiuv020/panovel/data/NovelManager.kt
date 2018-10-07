package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.download.DownloadingNotificationManager
import cc.aoeiuv020.panovel.local.LocalNovelProvider
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 持有小说对象，统一在这里读取小说相关数据，
 * 分担DataManager压力，
 *
 * 小说是已经加入数据库了的，有id的小说，
 *
 * Created by AoEiuV020 on 2018.06.12-21:49:11.
 */
class NovelManager(
        val novel: Novel,
        private val app: AppDatabaseManager,
        private val provider: NovelProvider,
        private val cache: CacheManager,
        // server可空，本地小说不传入server，相关方法也都不调用，
        private val server: ServerManager?,
        private val dnmLocal: ThreadLocal<DownloadingNotificationManager>
) : AnkoLogger {

    fun pinned() = app.pinned(novel)

    fun cancelPinned() = app.cancelPinned(novel)

    fun addToBookList(bookListId: Long) = app.addToBookList(bookListId, novel)
    fun removeFromBookList(bookListId: Long) = app.removeFromBookList(bookListId, novel)

    fun saveReadStatus() {
        novel.readTime = Date()
        updateReadStatus()
    }

    private fun updateReadStatus() = app.updateReadStatus(novel)

    fun getContentUrl(chapter: NovelChapter): String =
            provider.getContentUrl(chapter)

    fun novelContentsCached(): Collection<String> = cache.novelContentCached(novel)

    /**
     * 从缓存中读小说正文，没有就返回空，用于导入小说，
     */
    fun getContent(extra: String): List<String>? =
            cache.loadContent(novel, extra)

    fun requestContent(
            index: Int,
            chapter: NovelChapter,
            refresh: Boolean
    ): List<String> {
        // 指定刷新的话就不读缓存，
        if (!refresh) {
            cache.loadContent(novel, chapter.extra)?.also {
                return it
            }
        }
        val dnm: DownloadingNotificationManager = dnmLocal.get()
        dnm.downloadStart(novel, index, chapter.name)
        return provider.getNovelContent(chapter) { offset, length ->
            dnm.downloading(index, chapter.name, offset, length)
        }.also {
            dnm.downloadComplete(index, chapter.name)
            // 线程进度通知1秒后删除，
            // 如果还有剩，1秒内重新开始循环也就不会删除通知了，
            dnm.cancelNotification(TimeUnit.SECONDS.toMillis(1))
            // 缓存起来，
            cache.saveContent(novel, chapter.extra, it)
        }
    }

    // 指定刷新且是网络书源才读缓存，
    fun requestChapters(refresh: Boolean): List<NovelChapter> = if (refresh && provider !is LocalNovelProvider) {
        refreshChapters()
    } else {
        requireChapters()
    }

    private fun requireChapters(): List<NovelChapter> {
        // 确保存在详情页信息，
        requireDetail()
        // 先读取缓存，
        return cache.loadChapters(novel)
                ?: refreshChapters()
    }

    private fun refreshChapters(): List<NovelChapter> {
        // 确保存在详情页信息，
        requireDetail()
        val list = provider.requestNovelChapters()
        if (novel.readAtChapterName == Novel.VALUE_NULL) {
            // 如果数据库中没有阅读进度章节，说明没阅读过，直接存第一章名字，
            // 也可能是导入的进度，所以不能直接写0, 要用readAtChapterIndex，
            novel.readAtChapterName = list.getOrNull(novel.readAtChapterIndex)?.name ?: Novel.VALUE_NULL
        }
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.updateChapters(novel)
        cache.saveChapters(novel, list)
        // 这里异步，不影响刷新结果返回的时间，
        doAsync({ e ->
            val message = "上传<${novel.bookId}>刷新结果失败,"
            Reporter.post(message, e)
            error(message, e)
        }) {
            server?.touchUpdate(novel)
        }
        return list
    }


    fun requestDetail(refresh: Boolean) {
        if (refresh) {
            refreshDetail()
        } else {
            requireDetail()
        }
    }

    fun getDetailUrl(): String =
            provider.getDetailUrl()

    /**
     * 请求小说详情，也就是刷新，
     */
    private fun refreshDetail() {
        provider.updateNovelDetail()
        // 写入数据库，包括名字作者和extra都以详情页返回结果为准，
        app.updateDetail(novel)
    }

    /**
     * 确保小说详情存在，
     */
    private fun requireDetail() {
        debug { "requireNovelDetail $novel" }
        // chapters非空表示已经获取过小说详情了，
        if (novel.chapters != null) {
            return
        }
        refreshDetail()
    }

    fun updateBookshelf(checked: Boolean) {
        novel.bookshelf = checked
        app.db.novelDao().updateBookshelf(novel.nId, novel.bookshelf)
        // 向极光订阅/取消对应tag,
        if (novel.bookshelf) {
            server?.addTags(listOf(novel))
        } else {
            server?.removeTags(listOf(novel))
        }
    }

    fun cleanCache() {
        cache.clean(novel)
        provider.cleanCache()
    }

    fun cleanData() {
        cleanCache()
        provider.cleanData()
        app.clean(novel)
    }

    fun getImage(extra: String): URL {
        return provider.getImage(extra)
    }

}