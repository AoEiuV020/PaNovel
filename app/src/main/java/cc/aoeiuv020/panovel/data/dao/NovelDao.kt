package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.05.22-11:51:43.
 */
@Dao
abstract class NovelDao {
    @Query("select * from Novel where bookshelf = 1")
    abstract fun listBookshelf(): List<Novel>

    @Query("update Novel set bookshelf = :bookshelf where id = :id")
    abstract fun updateBookshelf(id: Long, bookshelf: Boolean)
}