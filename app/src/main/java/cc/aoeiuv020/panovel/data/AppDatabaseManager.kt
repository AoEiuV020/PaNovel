package cc.aoeiuv020.panovel.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.data.db.AppDatabase
import cc.aoeiuv020.panovel.data.entity.*
import cc.aoeiuv020.panovel.settings.OrderBy
import java.util.*

/**
 * 封装一个数据库多个表多个DAO的联用，
 *
 * Created by AoEiuV020 on 2018.04.27-11:52:55.
 */
class AppDatabaseManager(context: Context) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val db: AppDatabase = AppDatabase.getInstance(context)

    fun queryOrNewNovel(novelMinimal: NovelMinimal) = queryOrNewNovel(site = novelMinimal.site,
            author = novelMinimal.author, name = novelMinimal.name, detail = novelMinimal.detail)

    private fun queryOrNewNovel(site: String, author: String, name: String, detail: String): Novel = db.runInTransaction<Novel> {
        var exists = query(site, author, name)
        if (exists == null) {
            exists = query(site, detail)
        }
        if (exists != null) {
            // 如果查询到了，判断下detail是否一致，
            if (exists.detail != detail) {
                // 如果detail不一致，以晚得到的，也就是传入的参数detail为准，
                // 以防万一数据库中的detail无效时，可以通过再次模糊搜索，刷新detail,
                exists.detail = detail
                db.novelDao().updateDetailOnly(exists.nId, exists.detail)
            }
            return@runInTransaction exists
        } else {
            return@runInTransaction Novel(site = site, author = author, name = name, detail = detail).also {
                // 数据库里没有，需要插入，
                // 插入后确保novel要有这个id,
                it.id = db.novelDao().insert(it)
            }
        }
    }

    @Suppress("unused")
    fun queryOrNewSite(name: String, baseUrl: String, logo: String, enabled: Boolean, hide: Boolean): Site = db.runInTransaction<Site> {
        db.siteDao().query(name) ?: Site(
                name, baseUrl, logo, enabled, hide = hide
        ).also {
            db.siteDao().insert(it)
        }
    }

    fun newSite(name: String, baseUrl: String, logo: String, enabled: Boolean, hide: Boolean): Site = db.runInTransaction<Site> {
        Site(
            name, baseUrl, logo, enabled, hide = hide, createTime = Date()
        ).also {
            db.siteDao().insert(it)
        }
    }

    fun query(id: Long): Novel = db.novelDao().query(id)

    fun query(site: String, author: String, name: String): Novel? =
            db.novelDao().query(site, author, name)

    fun query(site: String, detail: String): Novel? =
            db.novelDao().query(site, detail)

    fun updateDetail(novel: Novel) = db.runInTransaction {
        // 兼容更新名字作者的情况，避免因改名成数据库已经存在的小说导致更新异常，
        // 这种情况改本地持有的小说对象,
        // 只是以防万一，在主要靠查询时的处理，
        val existsNovel = query(novel.site, novel.author, novel.name)
        if (existsNovel != null) {
            novel.id = existsNovel.id
        }
        db.novelDao().updateNovelDetail(
                novel.nId,
                novel.name, novel.author, novel.detail,
                novel.image, novel.introduction, novel.updateTime, novel.nChapters
        )
    }

    fun updateChapters(
            novel: Novel
    ) = db.novelDao().updateChapters(
            novel.nId, novel.chaptersCount,
            novel.readAtChapterName, novel.lastChapterName,
            novel.updateTime, novel.checkUpdateTime, novel.receiveUpdateTime
    )

    fun updateBookshelf(novel: Novel) =
            db.novelDao().updateBookshelf(novel.nId, novel.bookshelf)

    fun listBookshelf(orderBy: OrderBy): List<Novel> = when (orderBy) {
        OrderBy.Id -> db.novelDao().listBookshelfOrderById()
        OrderBy.ReadTime -> db.novelDao().listBookshelfOrderByReadTime()
        OrderBy.UpdateTime -> db.novelDao().listBookshelfOrderByReceiveUpdateTime()
        OrderBy.Smart -> db.novelDao().listBookshelfOrderBySmart()
        OrderBy.Name -> db.novelDao().listBookshelfOrderByName()
        OrderBy.Author -> db.novelDao().listBookshelfOrderByAuthor()
        OrderBy.Site -> db.novelDao().listBookshelfOrderBySite()
    }

    fun pinned(novel: Novel) = db.novelDao().updatePinnedTime(novel.nId, Date())
    fun cancelPinned(novel: Novel) = db.novelDao().updatePinnedTime(novel.nId, Date(0))
    fun updatePinnedTime(site: Site) = db.siteDao().updatePinnedTime(site.name, site.pinnedTime)

    fun updateReadStatus(novel: Novel) = db.novelDao().updateReadStatus(novel.nId,
            novel.readAtChapterIndex, novel.readAtTextIndex,
            novel.readAtChapterName, novel.readTime, novel.pinnedTime)

    fun siteEnabledChange(site: Site) = db.siteDao().updateEnabled(site.name, site.enabled)
    @Suppress("unused")
    fun siteHideChange(site: Site) = db.siteDao().updateEnabled(site.name, site.hide)

    fun history(historyCount: Int): List<Novel> = db.novelDao().history(historyCount)
    fun getBookList(bookListId: Long): BookList = db.bookListDao().queryBookList(bookListId)
    fun inBookList(bookListId: Long, list: List<Novel>): List<Boolean> = db.runInTransaction<List<Boolean>> {
        list.map {
            db.bookListDao().contains(bookListId, it.nId)
        }
    }

    fun isEmpty(): Boolean = !db.novelDao().isNotEmpty()

    fun addToBookList(bookListId: Long, novel: Novel) =
            db.bookListDao().insert(BookListItem(bookListId = bookListId, novelId = novel.nId))

    fun removeFromBookList(bookListId: Long, novel: Novel) =
            db.bookListDao().deleteItem(BookListItem(bookListId = bookListId, novelId = novel.nId))

    fun getNovelFromBookList(bookListId: Long): List<Novel> =
            db.bookListDao().queryNovel(bookListId)

    fun getNovelMinimalFromBookList(bookListId: Long): List<NovelMinimal> =
            db.bookListDao().queryNovelMinimal(bookListId)

    fun allBookList(): List<BookList> = db.bookListDao().list()
    fun renameBookList(bookList: BookList, name: String) =
            db.bookListDao().updateBookListName(bookList.nId, name)

    fun copyBookList(bookList: BookList, name: String) = db.runInTransaction {
        val newBookListId = newBookList(name)
        val list = db.bookListDao().queryNovel(bookList.nId)
        list.forEach {
            // 导入书单里的小说对象没有id, 要查一下，不存在就插入小说，
            val novel = queryOrNewNovel(NovelMinimal(it))
            if (!checkSiteSupport(novel)) {
                // 网站不在支持列表就不添加，
                // 基本信息已经写入数据库也无所谓了，
                return@forEach
            }
            // 然后再把这小说加入这书单，
            addToBookList(newBookListId, novel)
        }
    }

    fun removeBookList(bookList: BookList) = db.bookListDao().deleteList(bookList)

    /**
     * 书单直接插入，名字可以重复，uuid不能重复，
     */
    fun newBookList(name: String, uuid: String = UUID.randomUUID().toString()) =
            db.bookListDao().insert(BookList(id = null, name = name, createTime = Date(), uuid = uuid))

    /**
     * 创建新的书单或者清空已经存在的书单中的小说以添加新书，
     */
    private fun createOrResetBookList(name: String, uuid: String): Long {
        val bookList = db.bookListDao().queryBookListByUuid(uuid)
        return if (bookList == null) {
            newBookList(name, uuid)
        } else {
            db.bookListDao().resetBookList(bookList.nId)
            if (name != bookList.name) {
                renameBookList(bookList, name)
            }
            bookList.nId
        }
    }

    /**
     * 导入书单，先新建个书单，再一本本插入，
     */
    fun importBookList(name: String, list: List<NovelMinimal>, uuid: String) = db.runInTransaction {
        val bookListId = createOrResetBookList(name, uuid)
        list.forEach {
            // 导入书单里的小说对象没有id, 要查一下，不存在就插入小说，
            val novel = queryOrNewNovel(it)
            if (!checkSiteSupport(novel)) {
                // 网站不在支持列表就不添加，
                // 基本信息已经写入数据库也无所谓了，
                return@forEach
            }
            // 然后再把这小说加入这书单，
            addToBookList(bookListId, novel)
        }
    }

    fun removeBookshelf(bookList: BookList) = db.bookListDao().removeBookshelf(bookList.nId)

    fun addBookshelf(bookList: BookList) = db.bookListDao().addBookshelf(bookList.nId)

    fun checkSiteSupport(novel: Novel) = db.siteDao().checkSiteSupport(novel.site)

    fun cleanBookshelf() = db.novelDao().cleanBookshelf()

    fun cleanBookList() = db.bookListDao().cleanBookList()

    fun cleanHistory() = db.novelDao().cleanHistory()
    @Suppress("unused")
    fun updateSiteInfo(site: Site) = db.siteDao().updateSiteInfo(site.name, site.baseUrl, site.logo)

    fun hasUpdateNovelList(): List<Novel> = db.novelDao().hasUpdateNovelList()
    fun clean(novel: Novel) = db.novelDao().delete(novel)
    /**
     * 导入小说时，如果已经存在，就覆盖所有信息，
     */
    fun updateAll(novel: Novel) = db.novelDao().update(novel)

    /**
     * 返回需要备份进度的小说，
     * 也就是书架或者书单中有出现的，
     */
    fun exportNovelProgress(): List<Novel> {
        return db.novelDao().listImportant()
    }
}