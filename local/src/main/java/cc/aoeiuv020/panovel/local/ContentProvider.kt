package cc.aoeiuv020.panovel.local

import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.19-20:54:49.
 */
interface ContentProvider {
    fun getNovelContent(extra: String): List<String>

    // 封面默认存完整url, 但是epub要存包内相对路径，否则针对临时文件解析的封面不可用，
    fun getCoverImage(extra: String): URL = URL(extra)

}