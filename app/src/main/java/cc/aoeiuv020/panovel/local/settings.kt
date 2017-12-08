package cc.aoeiuv020.panovel.local

import android.content.res.ColorStateList
import android.net.Uri
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.reader.ReaderConfig

/**
 * 设置，
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */
object Settings : LocalSource {
    /**
     * 阅读界面点击退出全屏的延迟，
     * 有点延迟看着顺眼点，
     */
    var fullScreenDelay: Int by PrimitiveDelegate(300)
    var backPressOutOfFullScreen: Boolean by PrimitiveDelegate(false)
    var textSize: Int by PrimitiveDelegate(18)
    var lineSpacing: Int by PrimitiveDelegate(2)
    var paragraphSpacing: Int by PrimitiveDelegate(4)
    var leftSpacing: Int by PrimitiveDelegate(0)
    var topSpacing: Int by PrimitiveDelegate(0)
    var rightSpacing: Int by PrimitiveDelegate(0)
    var bottomSpacing: Int by PrimitiveDelegate(0)

    var textColor: Int by PrimitiveDelegate(0xff000000.toInt())
    var backgroundColor: Int by PrimitiveDelegate(0xffffffff.toInt())
    var backgroundImage: Uri? by UriDelegate()


    var historyCount: Int by PrimitiveDelegate(200)

    var asyncThreadCount: Int by PrimitiveDelegate(30)
    var downloadThreadCount: Int by PrimitiveDelegate(4)

    var adEnabled: Boolean by PrimitiveDelegate(true)
    var BookSmallLayout: Boolean by PrimitiveDelegate(true)


    var bookListAutoSave: Boolean by PrimitiveDelegate(true)

    var chapterColorDefault: Int by PrimitiveDelegate(0xff000000.toInt())
    var chapterColorCached: Int by PrimitiveDelegate(0xff00ff00.toInt())
    var chapterColorReadAt: Int by PrimitiveDelegate(0xffff0000.toInt())
    val chapterColorList
        get() = ColorStateList(
                arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked, android.R.attr.state_selected),
                        intArrayOf()
                ),
                intArrayOf(
                        chapterColorReadAt,
                        chapterColorCached,
                        chapterColorDefault
                )
        )

    var animMode: AnimMode? by GsonDelegate.new(null)

    fun makeReaderConfig() = ReaderConfig(
            textSize,
            lineSpacing,
            paragraphSpacing,
            leftSpacing,
            topSpacing,
            rightSpacing,
            bottomSpacing,
            textColor,
            backgroundColor,
            backgroundImage,
            animMode
    )
}

