package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * 封装多个数据库的联用，
 * 隐藏api模块的数据类，app只使用这里的数据库实体，
 *
 * Created by AoEiuV020 on 2018.04.28-16:53:14.
 */
object DataManager {
    lateinit var app: AppDatabaseManager
    lateinit var api: ApiManager
    @Synchronized
    fun init(ctx: Context) {
        if (!::app.isInitialized) {
            app = AppDatabaseManager(ctx)
        }
        if (!::api.isInitialized) {
            api = ApiManager(ctx)
        }
    }

    fun listBookshelf(): List<Novel> {
        return app.db.novelDao().listBookshelf()
    }

    fun updateBookshelf(novel: Novel) {
        app.db.novelDao().updateBookshelf(novel.nId, novel.bookshelf)
    }

    fun refreshChapters(novel: Novel) {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        val list = api.requestNovelChapters(novel)
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.db.novelDao().updateChapters(
                novel.nId, novel.chaptersCount,
                novel.readAtChapterName, novel.lastChapterName,
                novel.updateTime, novel.checkUpdateTime, novel.receiveUpdateTime
        )
        TODO("cache.save(list)")
    }

    private fun requireNovelDetail(novel: Novel) {
        // chapters非空表示已经获取过小说详情了，
        if (novel.chapters != null) {
            return
        }
        api.updateNovelDetail(novel)
        // 写入数据库，
        app.db.novelDao().updateNovelDetail(novel.nId, novel.image, novel.introduction, novel.updateTime)
    }

    fun getNovelDetail(id: Long): Novel {
        val novel = app.db.novelDao().query(id)
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        return novel
    }

    fun getDetailUrl(novel: Novel): String {
        return api.getDetailUrl(novel)
    }
}
