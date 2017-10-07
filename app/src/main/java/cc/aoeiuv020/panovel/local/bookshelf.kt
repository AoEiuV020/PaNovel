package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelDetail


/**
 * 书架，
 * Created by AoEiuV020 on 2017.10.04-19:23:01.
 */

object Bookshelf : LocalSource {
    fun contains(novelDetail: NovelDetail): Boolean = fileExists(novelDetail.bookId)

    fun add(novelDetail: NovelDetail) = fileSave(novelDetail.bookId, novelDetail)

    fun remove(novelDetail: NovelDetail) = fileRemove(novelDetail.bookId)

    fun list(): List<NovelDetail> = fileList()
}
