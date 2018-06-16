package cc.aoeiuv020.panovel.local

/**
 * Created by AoEiuV020 on 2018.06.15-20:05:51.
 */
abstract class LocalNovelContext(
        val parser: LocalNovelParser
) {
    private var prepared = false

    /**
     * 准备数据，多次调用只完整解析一次，
     */
    fun prepare() {
        if (!prepared) {
            parser.parse()
            prepared = true
        }
    }

    /**
     * 准备数据，每次调用都完整解析，
     */
    fun parse() {
        parser.parse()
    }

    abstract fun getNovelContext(extra: String): List<String>
}