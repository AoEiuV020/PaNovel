package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.interrupt
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.NovelProvider
import cc.aoeiuv020.panovel.data.entity.Novel
import java.io.File
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.13-15:38:49.
 */
class LocalNovelProvider(
        private val novel: Novel
) : NovelProvider {
    // detail直接保存文件全路径，/x/y/z
    private val file = File(novel.detail)
    private val type = LocalNovelType.values()
            .firstOrNull { it.suffix == novel.site }
            ?: interrupt("本地小说类型<${novel.site}>不支持")
    private val parser = when (type) {
        LocalNovelType.TEXT -> TextParser(file, charset(novel.nChapters))
        LocalNovelType.EPUB -> EpubParser(file)
    }

    override fun requestNovelChapters(): List<NovelChapter> {
        val info = parser.parse()
        update(novel, info)
        return info.chapters.map {
            NovelChapter(name = it.name, extra = it.extra)
        }
    }

    override fun getNovelContent(extra: String): List<String> {
        return parser.getNovelContent(extra)
    }

    override fun getContentUrl(extra: String): String {
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

    override fun clean() {
        file.delete()
    }

    companion object {
        // 因为要在导入时和刷新章节列表时调用，所以写在伴生对象里，
        fun update(novel: Novel, info: LocalNovelInfo) {
            novel.apply {
                checkUpdateTime = Date()
            }
            val list = info.chapters
            novel.apply {
                chaptersCount = list.size
                if (readAtChapterIndex == 0) {
                    // 阅读至第一章代表没阅读过，保存第一章的章节名，
                    readAtChapterName = list.firstOrNull()?.name ?: "(null)"
                }
                lastChapterName = list.lastOrNull()?.name ?: "(null)"
            }
        }
    }
}