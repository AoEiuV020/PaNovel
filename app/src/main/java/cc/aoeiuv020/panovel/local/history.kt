package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelDetail

/**
 *
 * Created by AoEiuV020 on 2017.10.10-17:59:51.
 */
object History : LocalSource {
    private var max: Int = 50

    private fun add(history: NovelHistory) = gsonSave(history.detail.bookId, history)

    fun add(detail: NovelDetail) = add(NovelHistory(detail))

    private fun remove(detail: NovelDetail) = gsonRemove(detail.bookId)

    private fun remove(history: NovelHistory) = remove(history.detail)

    fun list(): List<NovelHistory> = gsonList<NovelHistory>().sortedByDescending { it.date }.let { list ->
        // 删除过多的历史，
        val n = list.size - max
        if (n > 0) {
            list.subList(max, list.size).forEach { remove(it) }
            list.subList(0, max)
        } else {
            list
        }
    }
}