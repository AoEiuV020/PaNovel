package cc.aoeiuv020.panovel.local

/**
 * 调用parse方法解析后能拿出其他数据，
 *
 * Created by AoEiuV020 on 2018.06.15-19:06:15.
 */
interface LocalNovelParser {
    val type: LocalNovelType
    val author: String?
    val name: String?
    val introduction: String?
    /**
     * 章节列表不能null, 解析前可以给个空LinkedList，
     */
    val chapters: List<LocalNovelChapter>
    val requester: String?

    fun parse()
}