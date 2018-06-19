package cc.aoeiuv020.panovel.local

import java.io.File
import java.net.URL

/**
 * 调用parse方法解析后能拿出其他数据，
 *
 * Created by AoEiuV020 on 2018.06.15-19:06:15.
 */
abstract class LocalNovelParser(
        protected val file: File
) : ContentProvider {

    abstract fun parse(): LocalNovelInfo

    // 封面默认存完整url, 但是epub要存包内相对路径，否则针对临时文件解析的封面不可用，
    open fun getCoverImage(extra: String): URL = URL(extra)
}