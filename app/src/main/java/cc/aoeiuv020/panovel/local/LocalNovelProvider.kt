package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.NovelProvider
import cc.aoeiuv020.panovel.data.entity.Novel
import java.io.File

/**
 * Created by AoEiuV020 on 2018.06.13-15:38:49.
 */
abstract class LocalNovelProvider(
        protected val novel: Novel
) : NovelProvider {
    // detail直接保存文件全路径，/x/y/z
    protected val file = File(novel.detail)
    protected abstract val context: LocalNovelContext

    override fun getContentUrl(chapter: NovelChapter): String {
        return getDetailUrl()
    }

    override fun getDetailUrl(): String {
        return file.toURI().toString()
    }

    override fun updateNovelDetail() {
        // 不真的刷新什么，但是以防万一，chapters为null会反复调用这个方法，
        if (novel.chapters == null) {
            // 按理说没用，编码是一开始就决定好了写死的，不会为空，
            novel.chapters = "null"
        }
    }
}