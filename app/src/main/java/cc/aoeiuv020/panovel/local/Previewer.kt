package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.irondb.FileWrapper
import cc.aoeiuv020.panovel.api.NovelChapter
import java.nio.charset.Charset
import java.util.*

/**
 * 预览，导入前判断文件信息，
 * 是.txt还是.
 *
 * Created by AoEiuV020 on 2018.06.13-16:08:19.
 */
class Previewer(
        private val fileWrapper: FileWrapper,
        private val uri: String
) {

    // 先根据uri中可能存在的文件后缀判断，
    fun type() = if (uri.contains(LocalNovelType.TEXT.suffix)) {
        LocalNovelType.TEXT
    } else {
        null
    }

    fun charset(): Charset? {
        // TODO: 暂且只支持“知轩藏书”的GBK,
        return Charset.forName("GBK")
    }

    fun preview(type: LocalNovelType, charset: Charset): Info {
        return when (type) {
            LocalNovelType.TEXT -> fileWrapper.use {
                TextParser(it, charset).parse()
            }
            LocalNovelType.EPUB -> TODO()
        }
    }

    data class Info(
            val type: LocalNovelType,
            var author: String? = null,
            var name: String? = null,
            var introduction: String? = null,
            var chapters: LinkedList<NovelChapter>? = null
    )
}