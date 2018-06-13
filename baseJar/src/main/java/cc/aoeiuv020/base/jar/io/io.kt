package cc.aoeiuv020.base.jar.io

import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.13-19:12:58.
 */

fun BufferedRandomAccessFile.readLines(beginPos: Long, endPos: Long, charset: String): List<String> {
    seek(beginPos)
    val list = LinkedList<String>()
    while (filePointer < endPos) {
        readLine(charset)?.let {
            list.add(it)
        }
    }
    return list
}
