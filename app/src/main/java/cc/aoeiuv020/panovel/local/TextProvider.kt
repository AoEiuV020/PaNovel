package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.readLines
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.util.notNullOrReport
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

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
        return RandomAccessFile(file, "r").use { raf ->
            raf.readLines(beginPos, endPos, charset).map {
                it.trim()
            }
        }
    }

    override fun requestNovelChapters(): List<NovelChapter> {
        return TextParser(file, Charset.forName(novel.chapters))
                .parse().chapters.notNullOrReport()
    }

    override fun updateNovelDetail() {
        // 不真的刷新什么，
        if (novel.chapters == null) {
            // 按理说没用，编码是一开始就决定好了写死的，不会为空，
            novel.chapters = Charsets.UTF_8.name()
        }
    }
}