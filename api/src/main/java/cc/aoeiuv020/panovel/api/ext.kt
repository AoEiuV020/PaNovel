@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

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
 * 倒序删除重复章节，有的网站章节列表开头有倒叙的最新章节，
 * 987123456789 删除后得到 123456789,
 */
fun List<NovelChapter>.reverseRemoveDuplication(): List<NovelChapter> {
    // 以防万一，
    if (this.size == 1) return this
    // 倒序列表判断是否重复章节，
    val reversedList = this.asReversed()
    var index = 0
    return this.dropWhile {
        val asc = it
        val desc = reversedList[index]
        (asc.name == desc.name && asc.extra == desc.extra).also {
            ++index
        }
    }
}

