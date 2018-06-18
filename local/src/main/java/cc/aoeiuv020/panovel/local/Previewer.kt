package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.notNull
import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.epub.EpubReader
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
    fun guessCharset(type: LocalNovelType): String? {
        return when (type) {
            LocalNovelType.TEXT -> {
                file.inputStream()
            }
            LocalNovelType.EPUB -> {
                // 默认随便给个最常见的utf8编码，用的时候拿出来判断的是字节流，这里的编码不影响，
                EpubReader().readEpubLazy(ZipFile(file), Charsets.UTF_8.name())
                        // 拿出最重要的opf文件判断编码，
                        // 这里有先被EpubReader解析一次，浪费，但无所谓了，
                        .opfResource.inputStream
            }
        }.use { input ->
            FileCharsetDetector.guessStreamEncoding(input, FileCharsetDetector.SIMPLIFIED_CHINESE)
        }
    }

    fun parse(type: LocalNovelType, charset: Charset?): LocalNovelInfo {
        return when (type) {
            LocalNovelType.TEXT -> {
                TextParser(file, charset.notNull("charset"))
            }
            LocalNovelType.EPUB -> {
                EpubParser(file, charset.notNull("charset"))
            }
        }.parse()
    }
}