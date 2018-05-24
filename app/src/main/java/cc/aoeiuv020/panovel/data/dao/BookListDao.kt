package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.*
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.BookListItem
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.05.24-16:27:43.
 */
@Dao
abstract class BookListDao {

    @Query("select Novel.* from BookListItem left join Novel on BookListItem.novelId = Novel.id$1 and BookListItem.bookListId = :bookListId")
    abstract fun queryBookListLeftJoin(bookListId: Long): List<Novel>

    @Query("select 1 from BookListItem where bookListId = :bookListId and novelId = :novelId")
    abstract fun contains(bookListId: Long, novelId: Long): Boolean

    @Query("select id from BookList where name = :name")
    abstract fun queryBookListId(name: String): Long

    @Query("delete from BookListItem where bookListId = :bookListId and novelId = :novelId")
    abstract fun deleteIfExists(bookListId: Long, novelId: Long)

    /**
     * 操作太快可能重复，无视，
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(bookListItem: BookListItem)

    @Delete
    abstract fun delete(bookListItem: BookListItem)

    @Query("select * from BookList where id = :id")
    abstract fun queryBookList(id: Long): BookList
}