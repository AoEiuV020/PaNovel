package cc.aoeiuv020.panovel.local

import java.io.InputStream
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.19-20:54:49.
 */
interface ContentProvider {
    fun getNovelContent(chapter: LocalNovelChapter): List<String>

    // 封面默认存完整url, 但是epub要存包内相对路径，否则针对临时文件解析的图片不可用，
    fun getImage(extra: String): URL = URL(extra)

    // 从url中拿输入流，可以继承为后判断是否要提供这张图片，比如导出时没缓存的图片就不导出了，
    fun openImage(url: URL): InputStream? = url.openStream()
}