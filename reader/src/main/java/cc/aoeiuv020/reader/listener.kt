package cc.aoeiuv020.reader

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:17:55.
 */

interface TextRequester {
    fun request(index: Int, refresh: Boolean = false): List<String>
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

