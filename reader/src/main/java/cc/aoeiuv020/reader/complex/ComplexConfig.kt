package cc.aoeiuv020.reader.complex

import android.net.Uri
import cc.aoeiuv020.reader.ReaderConfig

/**
 *
 * Created by AoEiuV020 on 2017.12.01-22:12:19.
 */
class ComplexConfig(
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
) : ReaderConfig(textSize, lineSpacing, paragraphSpacing, leftSpacing, topSpacing, rightSpacing, bottomSpacing, textColor, backgroundColor, backgroundImage) {
    var pageMode: Int = 0
    var timeFormat: String = "HH:mm"
}
