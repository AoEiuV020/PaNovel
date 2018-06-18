package cc.aoeiuv020.reader

import android.widget.ImageView

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:17:55.
 */

interface TextRequester {
    fun requestChapter(index: Int, refresh: Boolean = false): List<String>
    // 小说章节正文由一个个段落构成，一个段落可能是一行文字，也可能是一张图片，
    // 支持图片就判断一下，否则直接toString,
    fun requestParagraph(string: String): Any

    // 请求图片，传入视图，
    // TODO: 改成传入回调，可以用于ImageView以外的地方，
    fun requestImage(image: Image, view: ImageView)
}

/**
 * 阅读进度监听器，
 * 进度改变时调用，
 */
interface ReadingListener {
    fun onReading(chapter: Int, text: Int)
}

// 这东西有点多余，换成点中心的回调就可以了，
interface MenuListener {
    fun hide()
    fun show()
    fun toggle()
}

