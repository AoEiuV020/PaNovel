package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelItem
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.10-17:59:51.
 */
object History : BaseLocalSource() {
    private val max get() = Settings.historyCount

    private fun add(history: NovelHistory) = gsonSave(history.novel.bookId.toString(), history)

    fun add(item: NovelItem) = add(NovelHistory(item, Date()))

    private fun remove(item: NovelItem) = gsonRemove(item.bookId.toString())

    private fun remove(history: NovelHistory) = remove(history.novel)

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

    fun clear() = gsonClear()
}