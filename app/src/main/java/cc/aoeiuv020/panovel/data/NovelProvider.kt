package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.panovel.api.NovelChapter

/**
 * 负责提供小说相关数据，
 *
 * Created by AoEiuV020 on 2018.06.13-12:58:30.
 */
interface NovelProvider {
    fun getContentUrl(extra: String): String
    fun getNovelContent(extra: String): List<String>
    fun requestNovelChapters(): List<NovelChapter>
    fun getDetailUrl(): String
    fun updateNovelDetail()
    fun clean()
}