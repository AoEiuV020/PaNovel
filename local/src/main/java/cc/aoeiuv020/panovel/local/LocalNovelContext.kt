package cc.aoeiuv020.panovel.local

/**
 * Created by AoEiuV020 on 2018.06.15-20:05:51.
 */
abstract class LocalNovelContext(
        val parser: LocalNovelParser
) {
    abstract fun getNovelContext(extra: String): List<String>
}