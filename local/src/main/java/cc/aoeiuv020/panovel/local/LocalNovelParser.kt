package cc.aoeiuv020.panovel.local

/**
 * 调用parse方法解析后能拿出其他数据，
 *
 * Created by AoEiuV020 on 2018.06.15-19:06:15.
 */
interface LocalNovelParser {

    fun parse(): LocalNovelInfo

    fun getNovelContent(extra: String): List<String>
}