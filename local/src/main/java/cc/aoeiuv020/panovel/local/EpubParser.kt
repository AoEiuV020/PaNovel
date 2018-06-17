package cc.aoeiuv020.panovel.local

import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.16-17:10:44.
 */
class EpubParser(
        file: File,
        private val charset: Charset
) : LocalNovelParser(file) {
    @Suppress("UNUSED_VARIABLE", "CanBeVal", "UNREACHABLE_CODE")
    override fun parse(): LocalNovelInfo {
        var author: String? = null
        var name: String? = null
        var image: String? = null
        var introduction: String? = null
        val chapters: MutableList<LocalNovelChapter> = LinkedList()
        // epub也需要指定编码，
        val requester: String = charset.name()

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        return LocalNovelInfo(author, name, image, introduction, chapters, requester)
    }

    override fun getNovelContent(extra: String): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
