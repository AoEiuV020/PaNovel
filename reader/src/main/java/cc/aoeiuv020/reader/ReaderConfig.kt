package cc.aoeiuv020.reader

import android.graphics.Typeface
import android.net.Uri
import cc.aoeiuv020.pager.IMargins

/**
 *
 * Created by AoEiuV020 on 2017.12.01-22:24:49.
 */
class ReaderConfig(
        textSize: Int,
        lineSpacing: Int,
        paragraphSpacing: Int,
        contentMargins: IMargins,
        textColor: Int,
        backgroundColor: Int,
        backgroundImage: Uri?,
        animationMode: AnimationMode = AnimationMode.SIMPLE,
        animationSpeed: Float = 0.8f,
        font: Typeface? = null,
        centerPercent: Float = 0.5f,
        /**
         * 这个不支持阅读中修改，省事，
         */
        var fullScreenClickNextPage: Boolean = false
) {
    internal var listeners = mutableListOf<ConfigChangedListener>()

    var textSize: Int = textSize
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.TextSize)
            }
        }
    var lineSpacing: Int = lineSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.LineSpacing)
            }
        }
    var paragraphSpacing: Int = paragraphSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.ParagraphSpacing)
            }
        }
    var contentMargins: IMargins = contentMargins
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.ContentSpacing)
            }
        }

    var textColor: Int = textColor
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.TextColor)
            }
        }
    var backgroundColor: Int = backgroundColor
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.BackgroundColor)
            }
        }
    var backgroundImage: Uri? = backgroundImage
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.BackgroundImage)
            }
        }
    var animationMode: AnimationMode = animationMode
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.AnimationMode)
            }
        }
    var animationSpeed: Float = animationSpeed
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.AnimDurationMultiply)
            }
        }
    var font: Typeface? = font
        set(value) {
            field = value
            titleFont = Typeface.create(value, Typeface.BOLD)
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.Font)
            }
        }
    /**
     * 修改字体时也要修改这个标题字体，
     */
    var titleFont: Typeface? = Typeface.create(font, Typeface.BOLD)
    var centerPercent: Float = centerPercent
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.CenterPercent)
            }
        }

}
