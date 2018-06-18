package cc.aoeiuv020.reader

import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.18-16:40:23.
 */

class Image(
        val url: URL
) {
    // 阅读器不支持的话可以调用toString直接展示文本，
    override fun toString(): String {
        return "![img]($url)"
    }
}

