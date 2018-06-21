package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.*
import cc.aoeiuv020.panovel.data.entity.Novel
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.22-11:51:43.
 */
@Dao
abstract class NovelDao {
    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, max(receiveUpdateTime, readTime) desc")
    abstract fun listBookshelfOrderBySmart(): List<Novel>

    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, readTime desc")
    abstract fun listBookshelfOrderByReadTime(): List<Novel>

    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, receiveUpdateTime desc")
    abstract fun listBookshelfOrderByReceiveUpdateTime(): List<Novel>

    // 越晚加入的小说id越大，结果越靠前，
    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, id desc")
    abstract fun listBookshelfOrderById(): List<Novel>

    // 没有中文拼音排序，
    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, name ")
    abstract fun listBookshelfOrderByName(): List<Novel>

    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, author ")
    abstract fun listBookshelfOrderByAuthor(): List<Novel>

    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc, site ")
    abstract fun listBookshelfOrderBySite(): List<Novel>

    @Query("update Novel set bookshelf = :bookshelf where id = :id")
    abstract fun updateBookshelf(id: Long, bookshelf: Boolean)

    /**
     * 返回非空，所以传入的id不能是不存在的，
     */
    @Query("select * from Novel where id = :id")
    abstract fun query(id: Long): Novel

    @Query("select * from Novel where site = :site and author = :author and name = :name")
    abstract fun query(site: String, author: String, name: String): Novel?

    @Query("update Novel set name = :name, author = :author, detail = :detail, image = :image, introduction = :introduction, updateTime = :updateTime, chapters = :chapters where id = :id")
    abstract fun updateNovelDetail(id: Long, name: String, author: String, detail: String, image: String, introduction: String, updateTime: Date, chapters: String)

    @Query("update Novel set chaptersCount = :chaptersCount, readAtChapterName = :readAtChapterName, lastChapterName = :lastChapterName, updateTime = :updateTime, checkUpdateTime = :checkUpdateTime, receiveUpdateTime = :receiveUpdateTime where id = :id")
    abstract fun updateChapters(
            id: Long, chaptersCount: Int,
            readAtChapterName: String, lastChapterName: String,
            updateTime: Date, checkUpdateTime: Date, receiveUpdateTime: Date
    )

    /**
     * 插入前都有查询，所以不用在插入失败时尝试更新，
     * 返回id, 要赋值回novel,
     */
    @Insert
    abstract fun insert(novel: Novel): Long

    @Query("update Novel set pinnedTime = :pinnedTime where id = :id")
    abstract fun updatePinnedTime(id: Long, pinnedTime: Date)

    @Query("update Novel set readAtChapterIndex = :readAtChapterIndex, readAtTextIndex = :readAtTextIndex, readAtChapterName = :readAtChapterName, readTime = :readTime where id = :id")
    abstract fun updateReadStatus(id: Long, readAtChapterIndex: Int, readAtTextIndex: Int, readAtChapterName: String, readTime: Date)

    // 筛阅读时间，不能是最小值，考虑到时区，无论怎么处理，给个一天的限制没问题，
    @Query("select * from Novel where readTime > 86400000 order by readTime desc limit :count")
    abstract fun history(count: Int): List<Novel>

    @Query("update Novel set bookshelf = 0")
    abstract fun cleanBookshelf()

    @Query("update Novel set readTime = 0")
    abstract fun cleanHistory()

    @Query("update Novel set detail = :detail where id = :id")
    abstract fun updateDetailOnly(id: Long, detail: String)

    @Query("select * from Novel where bookshelf = 1 and receiveUpdateTime > readTime order by receiveUpdateTime desc")
    abstract fun hasUpdateNovelList(): List<Novel>

    @Delete
    abstract fun delete(novel: Novel)

    @Update
    abstract fun update(novel: Novel)
}