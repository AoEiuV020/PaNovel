package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import cc.aoeiuv020.panovel.data.entity.Novel
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.22-11:51:43.
 */
@Dao
abstract class NovelDao {
    @Query("select * from Novel where bookshelf = 1 order by pinnedTime desc")
    abstract fun listBookshelf(): List<Novel>

    @Query("update Novel set bookshelf = :bookshelf where id$1 = :id")
    abstract fun updateBookshelf(id: Long, bookshelf: Boolean)

    /**
     * 返回非空，所以传入的id不能是不存在的，
     */
    // TODO: 为什么会有id$1,
    @Query("select * from Novel where id$1 = :id")
    abstract fun query(id: Long): Novel

    @Query("select * from Novel where site = :site and author = :author and name = :name")
    abstract fun query(site: String, author: String, name: String): Novel?

    @Query("update Novel set image = :image and introduction = introduction and updateTime = updateTime where id$1 = :id")
    abstract fun updateNovelDetail(id: Long, image: String, introduction: String, updateTime: Date)

    @Query("update Novel set chaptersCount = :chaptersCount and readAtChapterName = :readAtChapterName and lastChapterName = :lastChapterName and updateTime = :updateTime and checkUpdateTime = :checkUpdateTime and receiveUpdateTime = :receiveUpdateTime where id$1 = :id")
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

    @Query("update Novel set pinnedTime = :pinnedTime where id$1 = :id")
    abstract fun updatePinnedTime(id: Long, pinnedTime: Date)

    @Query("update Novel set readAtChapterIndex = :readAtChapterIndex and readAtTextIndex = :readAtTextIndex and readAtChapterName = :readAtChapterName and readTime = :readTime where id$1 = :id")
    abstract fun updateReadStatus(id: Long, readAtChapterIndex: Int, readAtTextIndex: Int, readAtChapterName: String, readTime: Date)

    @Query("select * from Novel order by readTime desc limit :count")
    abstract fun history(count: Int): List<Novel>
}