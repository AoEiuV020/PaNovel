package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.data.db.AppDatabase
import cc.aoeiuv020.panovel.data.entity.Novel
import java.util.*

/**
 * 封装一个数据库多个表多个DAO的联用，
 *
 * Created by AoEiuV020 on 2018.04.27-11:52:55.
 */
class AppDatabaseManager(context: Context) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val db: AppDatabase = AppDatabase.getInstance(context)

    fun queryOrNew(site: String, author: String, name: String, extra: String): Novel = db.runInTransaction<Novel> {
        db.novelDao().query(site, author, name)
                ?: Novel(id = null, site = site, author = author, name = name, detail = extra).also {
                    // 数据库里没有，需要插入，
                    // 插入后确保novel要有这个id,
                    it.id = db.novelDao().insert(it)
                }
    }

    fun query(id: Long): Novel =
            db.novelDao().query(id)

    fun query(site: String, author: String, name: String): Novel? =
            db.novelDao().query(site, author, name)

    fun updateNovelDetail(id: Long, image: String, introduction: String, updateTime: Date) =
            db.novelDao().updateNovelDetail(id, image, introduction, updateTime)

    fun updateChapters(
            id: Long, chaptersCount: Int,
            readAtChapterName: String, lastChapterName: String,
            updateTime: Date, checkUpdateTime: Date, receiveUpdateTime: Date
    ) = db.novelDao().updateChapters(id, chaptersCount, readAtChapterName, lastChapterName,
            updateTime, checkUpdateTime, receiveUpdateTime)

    fun updateBookshelf(id: Long, bookshelf: Boolean) =
            db.novelDao().updateBookshelf(id, bookshelf)

    fun listBookshelf(): List<Novel> =
            db.novelDao().listBookshelf()

    fun pinned(novel: Novel) {
        db.novelDao().updatePinnedTime(novel.nId, Date())
    }

    fun cancelPinned(novel: Novel) {
        db.novelDao().updatePinnedTime(novel.nId, Date(0))
    }
}