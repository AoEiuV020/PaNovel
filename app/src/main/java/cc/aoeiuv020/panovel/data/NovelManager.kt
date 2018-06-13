package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

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
        private val server: ServerManager?
) : AnkoLogger {

    fun pinned() = app.pinned(novel)

    fun cancelPinned() = app.cancelPinned(novel)

    fun addToBookList(bookListId: Long) = app.addToBookList(bookListId, novel)
    fun removeFromBookList(bookListId: Long) = app.removeFromBookList(bookListId, novel)

    /**
     * 询问服务器是否有更新，
     *
     * @return 返回true表示有更新，
     */
    fun askUpdate(): Boolean {
        debug { "askUpdate: <${novel.run { "$site.$author.$name.$receiveUpdateTime.$checkUpdateTime" }}>" }
        val result = server?.askUpdate(novel) ?: return false
        debug { "result: <${result.toJson()}}>" }
        return if (result.chaptersCount ?: 0 > novel.chaptersCount) {
            // 只对比章节数，
            debug { "has update ${result.chaptersCount} > ${novel.chaptersCount}" }
            true
        } else {
            debug { "no update ${result.chaptersCount} <= ${novel.chaptersCount}" }
            // 如果没更新，就保存服务器上的更新时间，如果更大的话，
            novel.apply {
                // 不更新receiveUpdateTime，不准，有时别人比较晚收到同一个更新然后推上去被拿到，
                checkUpdateTime = maxOf(checkUpdateTime, result.checkUpdateTime)
            }
            false
        }
    }


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
    fun getContent(chapter: NovelChapter): List<String>? =
            cache.loadContent(novel, chapter)

    fun requestContent(chapter: NovelChapter, refresh: Boolean): List<String> {
        // 指定刷新的话就不读缓存，
        if (!refresh) {
            cache.loadContent(novel, chapter)?.also {
                return it
            }
        }
        return provider.getNovelContent(chapter).also {
            // 缓存起来，
            cache.saveContent(novel, chapter, it)
        }
    }


    fun requestChapters(refresh: Boolean): List<NovelChapter> = if (refresh) {
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
        if (novel.readAtChapterName.isBlank()) {
            // 如果数据库中没有阅读进度章节，说明没阅读过，直接存第一章名字，
            // 也可能是导入的进度，所以不能直接写0, 要用readAtChapterIndex，
            novel.readAtChapterName = list.getOrNull(novel.readAtChapterIndex)?.name ?: ""
        }
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.updateChapters(novel)
        cache.saveChapters(novel, list)
        server?.touchUpdate(novel)
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

}