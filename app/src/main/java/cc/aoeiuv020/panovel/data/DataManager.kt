package cc.aoeiuv020.panovel.data

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.data.entity.Site
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * 封装多个数据库的联用，
 * 隐藏api模块的数据类，app只使用这里的数据库实体，
 *
 * Created by AoEiuV020 on 2018.04.28-16:53:14.
 */
object DataManager : AnkoLogger {
    lateinit var app: AppDatabaseManager
    lateinit var api: ApiManager
    @SuppressLint("StaticFieldLeak")
    lateinit var cookie: CookieManager
    lateinit var cache: CacheManager
    @Synchronized
    fun init(ctx: Context) {
        if (!::app.isInitialized) {
            app = AppDatabaseManager(ctx)
        }
        if (!::api.isInitialized) {
            api = ApiManager(ctx)
        }
        if (!::cookie.isInitialized) {
            cookie = CookieManager(ctx)
        }
        if (!::cache.isInitialized) {
            cache = CacheManager(ctx)
        }
    }

    fun listBookshelf(): List<Novel> = app.listBookshelf()

    fun updateBookshelf(novel: Novel) = app.updateBookshelf(novel.nId, novel.bookshelf)

    fun refreshChapters(novel: Novel): List<NovelChapter> {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        val list = api.requestNovelChapters(novel)
        // 不管是否真的有更新，都更新数据库，至少checkUpdateTime是必须要更新的，
        app.updateChapters(
                novel.nId, novel.chaptersCount,
                novel.readAtChapterName, novel.lastChapterName,
                novel.updateTime, novel.checkUpdateTime, novel.receiveUpdateTime
        )
        cache.saveChapters(novel, list)
        return list
    }

    fun requestChapters(novel: Novel): List<NovelChapter> {
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        // 先读取缓存，
        cache.loadChapters(novel)?.also {
            return it
        }
        return refreshChapters(novel)
    }

    private fun requireNovelDetail(novel: Novel) {
        debug { "requireNovelDetail $novel" }
        // chapters非空表示已经获取过小说详情了，
        if (novel.chapters != null) {
            return
        }
        requestDetail(novel)
    }

    private fun requestDetail(novel: Novel) {
        api.updateNovelDetail(novel)
        // 写入数据库，包括名字作者和extra都以详情页返回结果为准，
        app.db.novelDao().updateNovelDetail(novel.nId,
                novel.name, novel.author, novel.detail,
                novel.image, novel.introduction, novel.updateTime, novel.nChapters)
    }

    fun getNovel(id: Long): Novel = app.query(id)

    fun requestDetail(id: Long): Novel {
        val novel = app.query(id)
        requestDetail(novel)
        return novel
    }

    fun getNovelDetail(id: Long): Novel {
        val novel = app.query(id)
        // 确保存在详情页信息，
        requireNovelDetail(novel)
        return novel
    }

    fun getDetailUrl(novel: Novel): String =
            api.getDetailUrl(novel)

    fun getContentUrl(novel: Novel, chapter: NovelChapter): String =
            api.getContentUrl(novel, chapter)

    fun allNovelContexts() = api.contexts

    /**
     * 列出所有网站，
     */
    fun listSites(): List<Site> = app.db.siteDao().list()

    /**
     * 同步所有网站到数据库，app升级时调用一次就好，
     */
    fun syncSites(): List<Site> = app.db.runInTransaction<List<Site>> {
        allNovelContexts().map {
            it.site.run {
                app.queryOrNewSite(name, baseUrl, logo, enabled)
            }
        }
    }

    /**
     * @param author 作者名为空就不从数据库查询，
     * @param block 回调，对每个网站搜索结果进行操作，
     */
    fun search(name: String, author: String?, block: (List<Novel>) -> Unit) {
        listSites().filter(Site::enabled).forEach {
            search(it.name, name, author).also(block)
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
        val resultList = api.search(context, name)
        val novelList = app.db.runInTransaction<List<Novel>> {
            resultList.map {
                // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
                app.queryOrNewNovel(NovelMinimal(it))
            }
        }
        return novelList.filter {
            // 过滤，author为空表示模糊搜索，只要小说名包含，
            // author不为空表示精确搜索，要小说名和作者名都匹配，
            if (author == null) {
                it.name.contains(name)
            } else {
                it.name == name && it.author == author
            }
        }
    }

    fun getNovelContextByName(site: String) = api.getNovelContextByName(site)

    @MainThread
    fun pushCookiesToWebView(context: NovelContext) {
        // 高版本的设置cookie的回调乱七八糟的，用不上，
        context.cookies.forEach { (key, value) ->
            val cookiePair = "$key=$value"
            debug { "push cookie: <$cookie>" }
            context.cookieDomainList().forEach { domain ->
                cookie.putCookie(domain, cookiePair)
            }
        }
    }

    @WorkerThread
    fun syncCookies(ctx: Context?) = cookie.sync(ctx)

    /**
     * TODO: 这里有NovelContext拿到cookies后的文件操作，
     * 但是WebView cookies操作好像只能在主线程，
     * 索性都放主线程吧，不是很费时，
     */
    @MainThread
    fun pullCookiesFromWebView(context: NovelContext) {
        val cookiesMap = mutableMapOf<String, String>()
        context.cookieDomainList().forEach { domain ->
            cookie.getCookies(domain)
                    ?.split(';')?.map {
                        it.trim()
                    }?.mapNotNull { cookiePair ->
                        debug { "pull cookie: <$cookiePair>" }
                        try {
                            val index = cookiePair.indexOf('=')
                            val key = cookiePair.substring(0, index)
                            val value = cookiePair.substring(index + 1)
                            key to value
                        } catch (e: Exception) {
                            // 一个cookie处理错误直接无视，
                            val message = "cookie不合法，<$cookiePair>,"
                            Reporter.post(message, e)
                            error(message, e)
                            null
                        }
                    }?.toMap(cookiesMap)
        }
        context.putCookies(cookiesMap)
    }

    fun getNovelFromUrl(site: String, url: String): Novel {
        return api.getNovelFromUrl(getNovelContextByName(site), url).let {
            // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
            app.queryOrNewNovel(NovelMinimal(it))
        }
    }

    fun removeWebViewCookies() = cookie.removeCookies()

    fun removeNovelContextCookies(site: String) = api.removeCookies(getNovelContextByName(site))

    /**
     * 从缓存中读小说正文，没有就返回空，用于导入小说，
     */
    fun getContent(novel: Novel, chapter: NovelChapter): List<String>? =
            cache.loadContent(novel, chapter)

    fun requestContent(novel: Novel, chapter: NovelChapter, refresh: Boolean): List<String> {
        // 指定刷新的话就不读缓存，
        if (!refresh) {
            cache.loadContent(novel, chapter)?.also {
                return it
            }
        }
        return api.getNovelContent(novel, chapter).also {
            // 缓存起来，
            cache.saveContent(novel, chapter, it)
        }
    }

    fun pinned(novel: Novel) = app.pinned(novel)

    fun cancelPinned(novel: Novel) = app.cancelPinned(novel)

    fun updateReadStatus(novel: Novel) = app.updateReadStatus(novel)

    /**
     * @return 返回该小说已经缓存的章节列表，
     * 考虑到缓存可能对key进行了单向加密，
     * 只用于contains判断特定章节是否已经缓存，不用于读取章节信息，
     */
    fun novelContentsCached(novel: Novel): Collection<String> = cache.novelContentCached(novel)

    fun siteEnabledChange(site: Site) = app.siteEnabledChange(site)

    fun history(historyCount: Int): List<Novel> = app.history(historyCount)

    fun getBookList(bookListId: Long): BookList = app.getBookList(bookListId)

    /**
     * 列表中的小说在该书单里的包含情况，
     */
    fun inBookList(bookListId: Long, list: List<Novel>) = app.inBookList(bookListId, list)

    fun addToBookList(bookListId: Long, novel: Novel) = app.addToBookList(bookListId, novel)
    fun removeFromBookList(bookListId: Long, novel: Novel) = app.removeFromBookList(bookListId, novel)

    fun getNovelFromBookList(bookListId: Long): List<Novel> = app.getNovelFromBookList(bookListId)
    fun allBookList() = app.allBookList()
    fun renameBookList(bookList: BookList, name: String) = app.renameBookList(bookList, name)
    fun removeBookList(bookList: BookList) = app.removeBookList(bookList)
    fun newBookList(name: String) = app.newBookList(name)

    /**
     * @throws IllegalArgumentException 不支持的地址直接抛异常，
     */
    fun getNovelFromUrl(url: String): Novel = api.getNovelFromUrl(api.getNovelContextByUrl(url), url).let {
        // 搜索结果查询数据库看是否有这本，有就取出，没有就新建一个插入数据库，
        app.queryOrNewNovel(NovelMinimal(it))
    }

    fun importBookList(name: String, list: List<NovelMinimal>) = app.importBookList(name, list)
    fun addBookshelf(bookList: BookList) = app.addBookshelf(bookList)
    fun removeBookshelf(bookList: BookList) = app.removeBookshelf(bookList)

}
