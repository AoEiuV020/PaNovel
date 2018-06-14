package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.NovelProvider
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.06.13-15:38:49.
 */
abstract class LocalNovelProvider(
        private val novel: Novel
) : NovelProvider {
    override fun getContentUrl(chapter: NovelChapter): String {
        return getDetailUrl()
    }

    override fun getDetailUrl(): String {
        // detail直接保存文件全路径，/x/y/z
        return "file://${novel.detail}"
    }
}