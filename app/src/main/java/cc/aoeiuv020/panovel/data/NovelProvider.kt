package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.download.DownloadProgressListener
import java.net.URL

/**
 * 负责提供小说相关数据，
 *
 * Created by AoEiuV020 on 2018.06.13-12:58:30.
 */
interface NovelProvider {
    fun getContentUrl(chapter: NovelChapter): String
    fun getNovelContent(chapter: NovelChapter, listener: DownloadProgressListener?): List<String>
    fun requestNovelChapters(): List<NovelChapter>
    fun getDetailUrl(): String
    fun updateNovelDetail()
    fun cleanData()
    fun cleanCache()
    // 图片也要看上下文获取，比如epub内部，
    fun getImage(extra: String): URL
}