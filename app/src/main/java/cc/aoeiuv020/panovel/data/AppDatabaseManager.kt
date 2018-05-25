package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.data.db.AppDatabase
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.BookListItem
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.Site
import java.util.*

/**
 * 封装一个数据库多个表多个DAO的联用，
 *
 * Created by AoEiuV020 on 2018.04.27-11:52:55.
 */
class AppDatabaseManager(context: Context) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val db: AppDatabase = AppDatabase.getInstance(context)

    fun queryOrNewNovel(site: String, author: String, name: String, extra: String): Novel = db.runInTransaction<Novel> {
        db.novelDao().query(site, author, name)
                ?: Novel(id = null, site = site, author = author, name = name, detail = extra).also {
                    // 数据库里没有，需要插入，
                    // 插入后确保novel要有这个id,
                    it.id = db.novelDao().insert(it)
                }
    }

    fun queryOrNewSite(name: String, baseUrl: String, logo: String, enabled: Boolean): Site = db.runInTransaction<Site> {
        db.siteDao().query(name) ?: Site(
                name, baseUrl, logo, enabled
        ).also {
            db.siteDao().insert(it)
        }
    }

    fun query(id: Long): Novel = db.novelDao().query(id)

    fun query(site: String, author: String, name: String): Novel? =
            db.novelDao().query(site, author, name)

    fun updateNovelDetail(id: Long, detail: String, image: String, introduction: String, updateTime: Date) =
            db.novelDao().updateNovelDetail(id, detail, image, introduction, updateTime)

    fun updateChapters(
            id: Long, chaptersCount: Int,
            readAtChapterName: String, lastChapterName: String,
            updateTime: Date, checkUpdateTime: Date, receiveUpdateTime: Date
    ) = db.novelDao().updateChapters(id, chaptersCount, readAtChapterName, lastChapterName,
            updateTime, checkUpdateTime, receiveUpdateTime)

    fun updateBookshelf(id: Long, bookshelf: Boolean) =
            db.novelDao().updateBookshelf(id, bookshelf)

    fun listBookshelf(): List<Novel> = db.novelDao().listBookshelf()

    fun pinned(novel: Novel) = db.novelDao().updatePinnedTime(novel.nId, Date())

    fun cancelPinned(novel: Novel) = db.novelDao().updatePinnedTime(novel.nId, Date(0))

    fun updateReadStatus(novel: Novel) = db.novelDao().updateReadStatus(novel.nId,
            novel.readAtChapterIndex, novel.readAtTextIndex,
            novel.readAtChapterName, novel.readTime)

    fun siteEnabledChange(site: Site) = db.siteDao().updateEnabled(site.name, site.enabled)
    fun history(historyCount: Int): List<Novel> = db.novelDao().history(historyCount)
    fun getBookList(bookListId: Long): BookList = db.bookListDao().queryBookList(bookListId)
    fun inBookList(bookListId: Long, list: List<Novel>): List<Boolean> = db.runInTransaction<List<Boolean>> {
        list.map {
            db.bookListDao().contains(bookListId, it.nId)
        }
    }

    fun addToBookList(bookListId: Long, novel: Novel) =
            db.bookListDao().insert(BookListItem(bookListId = bookListId, novelId = novel.nId))

    fun removeFromBookList(bookListId: Long, novel: Novel) =
            db.bookListDao().deleteItem(BookListItem(bookListId = bookListId, novelId = novel.nId))

    fun getNovelFromBookShelf(bookListId: Long): List<Novel> =
            db.bookListDao().queryBookListLeftJoin(bookListId)

    fun allBookList(): List<BookList> = db.bookListDao().list()
    fun renameBookList(bookList: BookList, name: String) =
            db.bookListDao().updateBookListName(bookList.nId, name)

    fun removeBookList(bookList: BookList) = db.bookListDao().deleteList(bookList)

    fun newBookList(name: String) =
            db.bookListDao().insert(BookList(id = null, name = name, createTime = Date()))

}