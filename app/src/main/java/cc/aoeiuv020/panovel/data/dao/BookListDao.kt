package cc.aoeiuv020.panovel.data.dao

import androidx.room.*
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.BookListItem
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.NovelMinimal

/**
 * Created by AoEiuV020 on 2018.05.24-16:27:43.
 */
@Dao
abstract class BookListDao {

    @Query("select Novel.* from BookListItem left join Novel on BookListItem.novelId = Novel.id where BookListItem.bookListId = :bookListId order by pinnedTime desc, max(receiveUpdateTime, readTime) desc")
    abstract fun queryNovel(bookListId: Long): List<Novel>

    // 小数点.开头的是本地小说，不要，
    @Query("select Novel.site, Novel.author, Novel.name, Novel.detail  from BookListItem left join Novel on BookListItem.novelId = Novel.id where BookListItem.bookListId = :bookListId and Novel.site not like '.%'")
    abstract fun queryNovelMinimal(bookListId: Long): List<NovelMinimal>

    @Query("update Novel set bookshelf = 0 where id in (select novelId from BookListItem where bookListId = :bookListId)")
    abstract fun removeBookshelf(bookListId: Long)

    @Query("update Novel set bookshelf = 1 where id in (select novelId from BookListItem where bookListId = :bookListId)")
    abstract fun addBookshelf(bookListId: Long)

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

    @Query("delete from BookList")
    abstract fun cleanBookList()

}