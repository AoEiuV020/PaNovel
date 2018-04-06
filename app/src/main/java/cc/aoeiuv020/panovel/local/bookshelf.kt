package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelItem


/**
 * 书架，
 * Created by AoEiuV020 on 2017.10.04-19:23:01.
 */

@Suppress("MemberVisibilityCanPrivate")
object Bookshelf : BaseLocalSource() {
    private val listenerList = mutableSetOf<BookshelfModifyListener>()

    fun addListener(listener: BookshelfModifyListener) {
        listenerList.add(listener)
    }

    fun removeListener(listener: BookshelfModifyListener) {
        listenerList.remove(listener)
    }

    fun contains(novelItem: NovelItem): Boolean = gsonExists(novelItem.bookId.toString())

    fun add(novelItem: NovelItem) = gsonSave(novelItem.bookId.toString(), novelItem).also {
        listenerList.forEach {
            it.onBookshelfAdd(novelItem)
        }
    }

    fun remove(novelItem: NovelItem) = gsonRemove(novelItem.bookId.toString()).also {
        listenerList.forEach {
            it.onBookshelfRemove(novelItem)
        }

    }

    fun list(): List<NovelItem> = gsonList()
}

interface BookshelfModifyListener {
    fun onBookshelfAdd(novelItem: NovelItem)
    fun onBookshelfRemove(novelItem: NovelItem)
}
