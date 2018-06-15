package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.io.BufferedRandomAccessFile
import cc.aoeiuv020.base.jar.io.readLines
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.util.notNullOrReport
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.13-15:36:18.
 */
class TextProvider(
        private val novel: Novel
) : LocalNovelProvider(novel) {
    // 文件只读不写，不需要线程安全，
    private val file = File(novel.detail)

    override fun getNovelContent(chapter: NovelChapter): List<String> {
        val charset = Charset.forName(novel.chapters)
        val (beginPos, endPos) = chapter.extra.divide('/').let {
            it.first.toLong() to it.second.toLong()
        }
        return BufferedRandomAccessFile(file, "r").use { raf ->
            raf.readLines(beginPos, endPos, charset.name()).map {
                it.trim()
            }
        }
    }

    override fun requestNovelChapters(): List<NovelChapter> {
        return TextParser(file, Charset.forName(novel.chapters))
                .parse().also { update(novel, it) }
                .chapters.notNullOrReport()
                .map { NovelChapter(name = it.name, extra = it.extra) }
    }

    override fun updateNovelDetail() {
        // 不真的刷新什么，
        if (novel.chapters == null) {
            // 按理说没用，编码是一开始就决定好了写死的，不会为空，
            novel.chapters = Charsets.UTF_8.name()
        }
    }

    override fun clean() {
        file.delete()
    }

    companion object {
        fun update(novel: Novel, info: LocalNovelInfo) {
            novel.apply {
                introduction = info.introduction ?: "(null)"
                checkUpdateTime = Date()
            }
            // 不会为空，
            val list = info.chapters ?: return
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