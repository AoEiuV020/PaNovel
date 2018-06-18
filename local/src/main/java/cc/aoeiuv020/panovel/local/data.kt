package cc.aoeiuv020.panovel.local

/**
 * Created by AoEiuV020 on 2018.06.15-19:33:06.
 */
data class LocalNovelInfo(
        val author: String?,
        val name: String?,
        /**
         * 小说封面，
         */
        val image: String?,
        val introduction: String?,
        /**
         * 章节列表不能null, 解析前可以给个空LinkedList，
         */
        val chapters: List<LocalNovelChapter>,
        val requester: String?
)

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
 * 本地小说的类型，
 */
enum class LocalNovelType(
        val suffix: String
) {
    TEXT(".txt"), EPUB(".epub")
}
