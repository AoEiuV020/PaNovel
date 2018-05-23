package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.api.NovelContext
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
        return app.listBookshelf()
    }

    fun updateBookshelf(novel: Novel) =
            app.updateBookshelf(novel.nId, novel.bookshelf)

    fun refreshChapters(novel: Novel) {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        val list = api.requestNovelChapters(novel)
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.updateChapters(
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
        app.updateNovelDetail(novel.nId, novel.image, novel.introduction, novel.updateTime)
    }

    fun getNovelDetail(id: Long): Novel {
        val novel = app.query(id)
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        return novel
    }

    fun getDetailUrl(novel: Novel): String {
        return api.getDetailUrl(novel)
    }

    fun allNovelContexts() = api.contexts

    fun enabledNovelContexts(): List<NovelContext> {
        // 先取出关于网站是否启用的设置，
        val map = app.db.siteEnabledDao().list().map {
            it.name to it.enabled
        }.toMap()
        // 从所有网站过滤，
        return allNovelContexts().filter {
            map[it.site.name] ?: it.site.enabled
        }
    }


    /**
     * @param author 作者名为空就不从数据库查询，
     * @param block 回调，对每个网站搜索结果进行操作，
     */
    fun search(name: String, author: String?, block: (List<Novel>) -> Unit) {
        enabledNovelContexts().forEach {
            search(it, name, author).also(block)
        }
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     */
    fun search(site: String, name: String, author: String?): List<Novel> {
        if (author != null) {
            // 如果有作者名，那结果只可能有一个，
            // 如果数据库里有了，就直接返回，
            app.query(site, author, name)?.also {
                return listOf(it)
            }
        }
        val context = api.getNovelContextByName(site)
        return search(context, name, author)
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     */
    fun search(context: NovelContext, name: String, author: String?): List<Novel> {
        if (author != null) {
            // 如果有作者名，那结果只可能有一个，
            // 如果数据库里有了，就直接返回，
            app.query(context.site.name, author, name)?.also {
                return listOf(it)
            }
        }
        return api.search(context, name).map {
            // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
            app.queryOrNew(it.site, it.author, it.name, it.extra)
        }
    }
}
