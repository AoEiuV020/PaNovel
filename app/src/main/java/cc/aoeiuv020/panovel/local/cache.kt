package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelText
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2017.10.10-19:20:47.
 */
object Cache : LocalSource {
    private fun id(detail: NovelDetail, name: String) = id(detail.novel, name)
    private fun id(item: NovelItem, name: String) = "${item.bookId}/$name"

    fun putDetail(detail: NovelDetail) = gsonSave(id(detail, "detail"), detail)
    fun getDetail(item: NovelItem): NovelDetail? = gsonLoad(id(item, "detail"))

    private val chaptersCacheTimeout: Long = TimeUnit.DAYS.toMillis(1)
    fun putChapters(item: NovelItem, chapters: List<NovelChapter>) = gsonSave(id(item, "chapter"), chapters)
    /**
     * @param timeout 传入0表示不判断超时，
     */
    fun getChapters(item: NovelItem, timeout: Long = chaptersCacheTimeout): List<NovelChapter>? = gsonLoad(id(item, "chapter"), timeout)

    fun putText(item: NovelItem, chapter: NovelChapter, text: NovelText) = gsonSave(id(item, chapter.name), text)
    fun getText(item: NovelItem, chapter: NovelChapter): NovelText? = gsonLoad(id(item, chapter.name))
}