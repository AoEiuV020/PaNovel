package cc.aoeiuv020.base.jar

import java.io.ByteArrayOutputStream
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.13-19:12:58.
 */
fun RandomAccessFile.readLine(charset: Charset): String? {
    val input = ByteArrayOutputStream()
    var c = -1
    var eol = false

    while (!eol) {
        c = read()
        when (c) {
            -1, '\n'.toInt() -> eol = true
            '\r'.toInt() -> {
                eol = true
                val cur = filePointer
                if (read() != '\n'.toInt()) {
                    seek(cur)
                }
            }
            else -> input.write(c)
        }
    }
    return if (c == -1 && input.size() == 0) {
        null
    } else {
        input.toString(charset.name())
    }
}

fun RandomAccessFile.readLines(beginPos: Long, endPos: Long, charset: Charset): List<String> {
    seek(beginPos)
    val list = LinkedList<String>()
    while (filePointer < endPos) {
        readLine(charset)?.let {
            list.add(it)
        }
    }
    return list
}
