package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.irondb.FileWrapper
import org.mozilla.intl.chardet.nsPSMDetector
import java.nio.charset.Charset

/**
 * 预览，导入前判断文件信息，
 * 是.txt还是.epub,
 *
 * Created by AoEiuV020 on 2018.06.13-16:08:19.
 */
class Previewer(
        val fileWrapper: FileWrapper,
        private val uri: String
) {
    val type: LocalNovelType? = if (uri.contains(LocalNovelType.TEXT.suffix)) {
        LocalNovelType.TEXT
    } else {
        null
    }

    // 先根据uri中可能存在的文件后缀判断，
    fun type() = if (uri.contains(LocalNovelType.TEXT.suffix)) {
        LocalNovelType.TEXT
    } else {
        null
    }

    /**
     * 建议的编码，
     * 返回null表示不需要编码，比如epub，
     * 返回非null表示作为建议编码等待用户决定编码，
     */
    fun charset(type: LocalNovelType): String? {
        return when (type) {
            LocalNovelType.TEXT -> fileWrapper.use { file ->
                FileCharsetDetector().guessFileEncoding(file, nsPSMDetector.SIMPLIFIED_CHINESE)
                        ?: "unknown"
            }
            LocalNovelType.EPUB -> null
        }
    }

    fun preview(type: LocalNovelType, charset: Charset): LocalNovelInfo {
        return when (type) {
            LocalNovelType.TEXT -> fileWrapper.use {
                TextParser(it, charset).parse()
            }
            LocalNovelType.EPUB -> TODO()
        }
    }

    fun clean() {
        fileWrapper.delete()
    }

}