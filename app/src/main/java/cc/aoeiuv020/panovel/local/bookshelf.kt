package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelDetail


/**
 * 书架，
 * Created by AoEiuV020 on 2017.10.04-19:23:01.
 */

object Bookshelf : LocalSource {
    fun contains(novelDetail: NovelDetail): Boolean = primitiveExists(novelDetail.bookId)

    fun add(novelDetail: NovelDetail) = gsonSave(novelDetail.bookId, novelDetail)

    fun remove(novelDetail: NovelDetail) = primitiveRemove(novelDetail.bookId)

    fun list(): List<NovelDetail> = gsonList()
}
