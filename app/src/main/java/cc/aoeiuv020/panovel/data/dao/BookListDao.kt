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

    @Query("select Novel.* from BookListItem inner join Novel on BookListItem.novelId = Novel.id and BookListItem.bookListId = :bookListId")
    abstract fun queryBook(bookListId: Long): List<Novel>

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

    /**
     * 插入的只能是不带有主键id的，不可能重复，
     * 返回插入的书单id,
     */
    @Insert
    abstract fun insert(bookList: BookList): Long

    @Delete
    abstract fun deleteItem(bookListItem: BookListItem)

    @Delete
    abstract fun deleteList(bookList: BookList)

    @Query("select * from BookList where id = :id")
    abstract fun queryBookList(id: Long): BookList

    @Query("select * from BookList")
    abstract fun list(): List<BookList>

    @Query("update BookList set name = :name where id = :id")
    abstract fun updateBookListName(id: Long, name: String)
}