package cc.aoeiuv020.reader

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:17:55.
 */

interface TextRequester {
    fun request(index: Int, refresh: Boolean = false): Text
}

/**
 * 监听章节切换，
 */
interface ChapterChangeListener {
    fun onChapterChange()
}

interface MenuListener {
    fun hide()
    fun show()
    fun toggle()
}

