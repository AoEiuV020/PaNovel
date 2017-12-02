package cc.aoeiuv020.reader

import android.net.Uri
import cc.aoeiuv020.reader.simple.SimpleConfigChangedListener

/**
 *
 * Created by AoEiuV020 on 2017.12.01-22:24:49.
 */
abstract class ReaderConfig(
        textSize: Int,
        lineSpacing: Int,
        paragraphSpacing: Int,
        leftSpacing: Int,
        topSpacing: Int,
        rightSpacing: Int,
        bottomSpacing: Int,
        textColor: Int,
        backgroundColor: Int,
        backgroundImage: Uri?
) {
    internal var listener: SimpleConfigChangedListener? = null
    var textSize: Int = textSize
        set(value) {
            field = value
            listener?.onTextSizeChanged()
        }
    var lineSpacing: Int = lineSpacing
        set(value) {
            field = value
            listener?.onLineSpacingChanged()
        }
    var paragraphSpacing: Int = paragraphSpacing
        set(value) {
            field = value
            listener?.onParagraphSpacingChanged()
        }
    var leftSpacing: Int = leftSpacing
        set(value) {
            field = value
            listener?.onLeftSpacingChanged()
        }
    var topSpacing: Int = topSpacing
        set(value) {
            field = value
            listener?.onTopSpacingChanged()
        }
    var rightSpacing: Int = rightSpacing
        set(value) {
            field = value
            listener?.onRightSpacingChanged()
        }
    var bottomSpacing: Int = bottomSpacing
        set(value) {
            field = value
            listener?.onBottomSpacingChanged()
        }

    var textColor: Int = textColor
        set(value) {
            field = value
            listener?.onTextColorChanged()
        }
    var backgroundColor: Int = backgroundColor
        set(value) {
            field = value
            listener?.onBackgroundColorChanged()
        }
    var backgroundImage: Uri? = backgroundImage
        set(value) {
            field = value
            listener?.onBackgroundImageChanged()
        }
}
