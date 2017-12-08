package cc.aoeiuv020.reader

import android.net.Uri
import cc.aoeiuv020.pager.animation.Margins

/**
 *
 * Created by AoEiuV020 on 2017.12.01-22:24:49.
 */
class ReaderConfig(
        textSize: Int,
        lineSpacing: Int,
        paragraphSpacing: Int,
        leftSpacing: Int,
        topSpacing: Int,
        rightSpacing: Int,
        bottomSpacing: Int,
        textColor: Int,
        backgroundColor: Int,
        backgroundImage: Uri?,
        animationMode: AnimationMode = AnimationMode.SIMPLE
) {
    internal var listeners = mutableListOf<ConfigChangedListener>()
    val margins: Margins
        get() = Margins(leftSpacing, topSpacing, rightSpacing, bottomSpacing)

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
    var leftSpacing: Int = leftSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.LeftSpacing)
            }
        }
    var topSpacing: Int = topSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.TopSpacing)
            }
        }
    var rightSpacing: Int = rightSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.RightSpacing)
            }
        }
    var bottomSpacing: Int = bottomSpacing
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.BottomSpacing)
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
}
