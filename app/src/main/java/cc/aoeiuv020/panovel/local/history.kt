package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelDetail

/**
 *
 * Created by AoEiuV020 on 2017.10.10-17:59:51.
 */
object History : LocalSource {
    fun add(history: NovelHistory) = gsonSave(history.detail.bookId, history)

    fun add(detail: NovelDetail) = add(NovelHistory(detail))

    @Suppress("unused")
    fun remove(history: NovelHistory) = gsonRemove(history.detail.bookId)

    fun list(): List<NovelHistory> = gsonList()
}