@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import java.io.FilterInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2017.10.02-16:01:09.
 */

/**
 * 结尾不要斜杆/，因为有的地址可能整数后面接文件后缀.html,
 * 开头要有斜杆/，因为有的网站可能host有整数，
 */
const val firstIntPattern: String = "/(\\d+)"
const val firstTwoIntPattern: String = "/(\\d+/\\d+)"
const val firstThreeIntPattern: String = "/(\\d+/\\d+/\\d+)"

/**
 * 倒序删除重复章节，有的网站章节列表开头有倒序的最新章节，
 * 987123456789 删除后得到 123456789,
 */
fun List<NovelChapter>.reverseRemoveDuplication(): List<NovelChapter> {
    // 以防万一，
    if (this.size == 1) return this
    // 倒序列表判断是否重复章节，
    val first = this.first()
    // 可能有延迟，开头的最新章节列表可能不是最新，先反着删除不是第一章的，再正着删除和最后一章一致的，
    val reversedList = this.dropLastWhile { desc ->
        !(first.name == desc.name && first.extra == desc.extra)
    }.asReversed()
    if (reversedList.size == 1) return this
    var index = 0
    return this.dropWhile { asc ->
        val desc = reversedList[index]
        (asc.name == desc.name && asc.extra == desc.extra).also {
            ++index
        }
    }
}

fun InputStream.copyTo(
        out: OutputStream,
        maxSize: Long = 0L,
        listener: ((Long, Long) -> Unit)? = null,
        bufferSize: Int = 4 * 1024
): Long {
    var bytesCopied: Long = 0
    listener?.invoke(bytesCopied, maxSize)
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        listener?.invoke(bytesCopied, maxSize)
        bytes = read(buffer)
    }
    return bytesCopied
}

class LoggerInputStream(
        input: InputStream,
        private val maxSize: Long = 0L,
        private val listener: ((Long, Long) -> Unit)? = null
) : FilterInputStream(input) {
    private var bytesCopied: Long = 0

    init {
        listener?.invoke(bytesCopied, maxSize)
    }

    override fun read(): Int {
        return super.read().also {
            if (it > 0) {
                bytesCopied++
                listener?.invoke(bytesCopied, maxSize)
            }
        }
    }

    // FilterInputStream的这个方法就是调用自己的read，不能重复处理了，
/*
    override fun read(b: ByteArray): Int = read(b, 0, b.size)
*/

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return super.read(b, off, len).also {
            if (it > 0) {
                bytesCopied += it
                listener?.invoke(bytesCopied, maxSize)
            }
        }
    }
}