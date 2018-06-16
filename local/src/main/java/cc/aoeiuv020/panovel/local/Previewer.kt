package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.panovel.local.text.FileCharsetDetector
import cc.aoeiuv020.panovel.local.text.TextParser
import java.io.File
import java.nio.charset.Charset

/**
 * 预览，导入前判断文件信息，
 * 是.txt还是.epub,
 *
 * Created by AoEiuV020 on 2018.06.13-16:08:19.
 */
class Previewer(
        val file: File,
        /**
         * 安卓上，可能先把Uri复制到私有目录下，
         * 这时file文件名就没有代表性，
         * 顺便把原始的uri也传进来方便判断，
         */
        private val uri: String = file.toURI().toString()
) {
    var charset: Charset? = null

    /**
     * 先根据uri中可能存在的文件后缀判断，
     *
     * 不赋值type, 决定文件类型后需要从外面主动赋值type,
     */
    fun guessType() = LocalNovelType.values().firstOrNull {
        uri.contains(it.suffix)
    }

    /**
     * 外面判断是否是txt纯文本小说，是纯文本才需要调用这个猜编码，作为默认值请求用户输入决定编码，
     * @return 返回建议的编码，可能是多个，逗号,分隔，

     */
    fun guessCharset(): String? {
        return FileCharsetDetector.guessFileEncoding(file, FileCharsetDetector.SIMPLIFIED_CHINESE)
    }

    fun parse(type: LocalNovelType): LocalNovelInfo {
        return when (type) {
            LocalNovelType.TEXT -> {
                val charset = charset.notNull("charset")
                TextParser(file, charset)
            }
            LocalNovelType.EPUB -> TODO()
        }.parse()
    }
}