package cc.aoeiuv020.panovel.local

import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.15-19:33:06.
 */
/**
 * 本地小说的章节，
 */
data class LocalNovelChapter(
        /**
         * 章节名不包括小说名，
         */
        val name: String,
        val extra: String
)

/**
 * 本地小说相关信息，
 * 因为是解析过程中一个一个获取的，所以属性要可空，
 */
data class LocalNovelInfo(
        val type: LocalNovelType,
        var author: String? = null,
        var name: String? = null,
        var introduction: String? = null,
        var chapters: LinkedList<LocalNovelChapter>? = null,
        var requester: String? = null
)

/**
 * 本地小说的类型，
 */
enum class LocalNovelType(
        val suffix: String
) {
    TEXT(".txt"), EPUB(".epub")
}
