package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import cc.aoeiuv020.panovel.data.entity.Novel
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.22-11:51:43.
 */
@Dao
abstract class NovelDao {
    @Query("select * from Novel where bookshelf = 1")
    abstract fun listBookshelf(): List<Novel>

    @Query("update Novel set bookshelf = :bookshelf where id$1 = :id")
    abstract fun updateBookshelf(id: Long, bookshelf: Boolean)

    // TODO: 为什么会有id$1,
    @Query("select * from Novel where id$1 = :id")
    abstract fun query(id: Long): Novel

    @Query("update Novel set image = :image and introduction = introduction and updateTime = updateTime where id$1 = :id")
    abstract fun updateNovelDetail(id: Long, image: String, introduction: String, updateTime: Date)

    @Query("update Novel set chaptersCount = :chaptersCount and readAtChapterName = :readAtChapterName and lastChapterName = :lastChapterName and updateTime = :updateTime and checkUpdateTime = :checkUpdateTime and receiveUpdateTime = :receiveUpdateTime where id$1 = :id")
    abstract fun updateChapters(
            id: Long, chaptersCount: Int,
            readAtChapterName: String, lastChapterName: String,
            updateTime: Date, checkUpdateTime: Date, receiveUpdateTime: Date
    )
}