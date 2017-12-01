package cc.aoeiuv020.reader

import io.reactivex.Observable

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:17:55.
 */

interface TextRequester {
    fun request(index: Int, refresh: Boolean): Observable<Text>
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

internal interface ConfigChangedListener {
    fun onTextSizeChanged()
    fun onTextColorChanged()

    fun onBackgroundColorChanged()
    fun onBackgroundImageChanged()

    fun onLineSpacingChanged()
    fun onParagraphSpacingChanged()

    fun onLeftSpacingChanged()
    fun onTopSpacingChanged()
    fun onRightSpacingChanged()
    fun onBottomSpacingChanged()
}
