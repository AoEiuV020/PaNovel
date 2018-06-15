package cc.aoeiuv020.panovel.local.text

import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.io.BufferedRandomAccessFile
import cc.aoeiuv020.base.jar.io.readLines
import cc.aoeiuv020.panovel.local.LocalNovelContext
import java.io.File
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.15-20:06:01.
 */
class TextContext(
        private val file: File,
        private val charset: Charset
) : LocalNovelContext(TextParser(file, charset)) {
    override fun getNovelContext(extra: String): List<String> {
        val (beginPos, endPos) = extra.divide('/').let {
            it.first.toLong() to it.second.toLong()
        }
        return BufferedRandomAccessFile(file, "r").use { raf ->
            // map去掉段首空格，顺便转成随机访问的ArrayList,
            raf.readLines(beginPos, endPos, charset.name()).map {
                it.trim()
            }
        }
    }
}