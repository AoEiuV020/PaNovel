package cc.aoeiuv020.panovel.settings

import android.graphics.Typeface
import android.net.Uri
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.reader.AnimationMode
import cc.aoeiuv020.reader.ReaderConfig

/**
 * Created by AoEiuV020 on 2018.05.26-17:10:57.
 */
object ReaderSettings : Pref {
    override val name: String
        get() = "Reader"

    var fullScreenClickNextPage: Boolean by Delegates.boolean(false)
    var volumeKeyScroll: Boolean by Delegates.boolean(true)
    var centerPercent: Float by Delegates.float(0.5f)
    // 亮度，0-255, 负数代表亮度跟随系统，
    var brightness: Int by Delegates.int(-1)
    /**
     * 阅读界面点击退出全屏的延迟，
     * 有点延迟看着顺眼点，
     */
    var fullScreenDelay: Int by Delegates.int(300)
    var backPressOutOfFullScreen: Boolean by Delegates.boolean(false)
    var textSize: Int by Delegates.int(26)
    var lineSpacing: Int by Delegates.int(13)
    var paragraphSpacing: Int by Delegates.int(0)

    /**
     * 小说内容的留白，
     */
    val contentMargins: Margins = Margins("ContentMargins", true, 1, 3, 1, 3)
    val paginationMargins: Margins = Margins("PaginationMargins", true, -1, -1, 1, 1)
    val bookNameMargins: Margins = Margins("BookNameMargins", true, 50, -1, -1, 1)
    val chapterNameMargins: Margins = Margins("ChapterNameMargins", true, 1, 1, -1, -1)
    val timeMargins: Margins = Margins("TimeMargins", true, -1, 1, 1, -1)
    val batteryMargins: Margins = Margins("BatteryMargins", true, 1, -1, -1, 1)
    /**
     * 对应上面几个，也就是页眉页脚那些信息的字体大小，
     */
    var messageSize: Int by Delegates.int(12)
    var autoRefreshInterval: Int by Delegates.int(60)
    var dateFormat: String by Delegates.string("HH:mm")
    var textColor: Int by Delegates.int(0xff000000.toInt())
    var font: Uri? by Delegates.uri()
    val tfFont: Typeface?
        get() = font?.let {
            try {
                Typeface.createFromFile(it.path)
            } catch (e: Exception) {
                // 文件损坏的情况，
                Reporter.post("字体生成失败", e)
                null
            }
        }
    var backgroundColor: Int by Delegates.int(0xffffe3aa.toInt())
    var backgroundImage: Uri? by Delegates.uri()
    var animationMode: AnimationMode by Delegates.enum(AnimationMode.SIMULATION)
    var animationSpeed: Float by Delegates.float(0.8f)
    fun makeReaderConfig() = ReaderConfig(
            textSize,
            lineSpacing,
            paragraphSpacing,
            contentMargins,
            paginationMargins,
            bookNameMargins,
            chapterNameMargins,
            timeMargins,
            batteryMargins,
            messageSize,
            dateFormat,
            textColor,
            backgroundColor,
            backgroundImage,
            animationMode,
            animationSpeed,
            tfFont,
            centerPercent,
            autoRefreshInterval,
            fullScreenClickNextPage
    )
}