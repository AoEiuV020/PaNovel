package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.panovel.data.entity.Novel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

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
        private val api: ApiManager,
        private val cache: CacheManager,
        private val server: ServerManager
) : AnkoLogger {
    fun requestDetail(refresh: Boolean) {
        if (refresh) {
            requestDetail(novel)
        } else {
            requireDetail(novel)
        }
    }

    /**
     * 请求小说详情，也就是刷新，
     */
    private fun requestDetail(novel: Novel) {
        api.updateNovelDetail(novel)
        // 写入数据库，包括名字作者和extra都以详情页返回结果为准，
        app.db.novelDao().updateNovelDetail(novel.nId,
                novel.name, novel.author, novel.detail,
                novel.image, novel.introduction, novel.updateTime, novel.nChapters)
    }

    /**
     * 确保小说详情存在，
     */
    private fun requireDetail(novel: Novel) {
        debug { "requireNovelDetail $novel" }
        // chapters非空表示已经获取过小说详情了，
        if (novel.chapters != null) {
            return
        }
        requestDetail(novel)
    }

    fun getDetailUrl(): String =
            api.getDetailUrl(novel)

    fun updateBookshelf(checked: Boolean) {
        novel.bookshelf = checked
        app.db.novelDao().updateBookshelf(novel.nId, novel.bookshelf)
        // 向极光订阅/取消对应tag,
        if (novel.bookshelf) {
            server.addTags(listOf(novel))
        } else {
            server.removeTags(listOf(novel))
        }
    }

}