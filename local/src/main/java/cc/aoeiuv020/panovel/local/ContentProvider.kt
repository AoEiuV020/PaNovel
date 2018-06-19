package cc.aoeiuv020.panovel.local

/**
 * Created by AoEiuV020 on 2018.06.19-20:54:49.
 */
interface ContentProvider {
    fun getNovelContent(extra: String): List<String>
}