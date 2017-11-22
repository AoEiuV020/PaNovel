package cc.aoeiuv020.panovel.local

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:38:30.
 */
object BookList : LocalSource {
    fun new(name: String)
            = add(BookListData(name))

    fun remove(bookListData: BookListData)
            = gsonRemove(bookListData.name)

    fun add(bookListData: BookListData)
            = gsonSave(bookListData.name, bookListData)

    fun get(name: String): BookListData?
            = gsonLoad(name)

    fun list(): List<BookListData>
            = gsonList()
}