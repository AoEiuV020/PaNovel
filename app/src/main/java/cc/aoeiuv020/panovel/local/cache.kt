package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelText

/**
 *
 * Created by AoEiuV020 on 2017.10.10-19:20:47.
 */
object Cache : LocalSource {
    private fun id(detail: NovelDetail, name: String) = id(detail.novel, name)
    private fun id(item: NovelItem, name: String) = "${item.bookId}/$name"

    fun putDetail(detail: NovelDetail) = gsonSave(id(detail, "detail"), detail)
    fun getDetail(item: NovelItem): NovelDetail? = gsonLoad(id(item, "detail"))

    private fun putChapters(item: NovelItem, chapters: NovelChapters) = gsonSave(id(item, "chapter"), chapters)
    fun putChapters(item: NovelItem, chapters: List<NovelChapter>) = putChapters(item, NovelChapters(chapters))
    fun getChapters(item: NovelItem): List<NovelChapter>? = gsonLoad<NovelChapters>(id(item, "chapter"))?.chapters

    fun putText(item: NovelItem, chapter: NovelChapter, text: NovelText) = gsonSave(id(item, chapter.name), text)
    fun getText(item: NovelItem, chapter: NovelChapter): NovelText? = gsonLoad(id(item, chapter.name))
}