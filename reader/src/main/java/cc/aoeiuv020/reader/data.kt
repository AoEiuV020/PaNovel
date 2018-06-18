package cc.aoeiuv020.reader

import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.18-16:40:23.
 */

class Image(
        // 参考glide, 各种可以得到图片的model, 不局限于URL,
        // 仔细想想好像没什么必要，URL能带URLStreamHandler, 可以自己决定从url得到输入流的方法，
        val url: URL
) {
    // 阅读器不支持的话可以调用toString直接展示文本，
    override fun toString(): String {
        return "![img]($url)"
    }
}

