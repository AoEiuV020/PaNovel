package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.readLines
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.13-15:36:18.
 */
class TextProvider(
        private val novel: Novel,
        file: File
) : LocalNovelProvider(file) {

    override fun getNovelContent(chapter: NovelChapter): List<String> {
        val charset = Charset.forName(novel.detail)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateNovelDetail() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}