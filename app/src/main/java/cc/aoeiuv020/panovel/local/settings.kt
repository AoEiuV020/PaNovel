package cc.aoeiuv020.panovel.local

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import cc.aoeiuv020.reader.AnimationMode
import cc.aoeiuv020.reader.ReaderConfig
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

/**
 * 设置，
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */
object Settings : BaseLocalSource(), AnkoLogger {
    /**
     * 这名字好长，小红点提示未读或新章节，
     * false则提示未读，也就是阅读进度不在最新章节，
     * true则提示新章节，也就是上次阅读后有更新，
     */
    var bookshelfRedDotNotifyNotReadOrNewChapter: Boolean by PrimitiveDelegate(false)
    var bookshelfRedDotSize: Float by PrimitiveDelegate(24f)
    var bookshelfRedDotColor: Int by PrimitiveDelegate(0xffff0000.toInt())
    var bookshelfShowMoreActionDot: Boolean by PrimitiveDelegate(true)
    var bookshelfAutoRefresh: Boolean by PrimitiveDelegate(false)

    /**
     * 阅读界面点击退出全屏的延迟，
     * 有点延迟看着顺眼点，
     */
    var fullScreenDelay: Int by PrimitiveDelegate(300)
    var backPressOutOfFullScreen: Boolean by PrimitiveDelegate(false)
    var textSize: Int by PrimitiveDelegate(18)
    var lineSpacing: Int by PrimitiveDelegate(2)
    var paragraphSpacing: Int by PrimitiveDelegate(4)

    /**
     * 小说内容的留白，
     */
    val contentMargins: Margins = Margins("ContentMargins")
    private val leftSpacing: Int by PrimitiveDelegate(0)
    private val topSpacing: Int by PrimitiveDelegate(0)
    private val rightSpacing: Int by PrimitiveDelegate(0)
    private val bottomSpacing: Int by PrimitiveDelegate(0)

    init {
        // 迁移1.3.11及以前留白设置，
        // 之后删了吧，跨版本的大不了作废这几个设置，
        contentMargins.takeIf {
            it.left == 0 && it.right == 0 && it.top == 0 && it.bottom == 0
        }?.apply {
            leftSpacing.takeUnless { it == 0 }?.let { left = it }
            rightSpacing.takeUnless { it == 0 }?.let { right = it }
            topSpacing.takeUnless { it == 0 }?.let { top = it }
            bottomSpacing.takeUnless { it == 0 }?.let { bottom = it }
        }
    }

    val paginationMargins: Margins = Margins("PaginationMargins")
    val bookNameMargins: Margins = Margins("BookNameMargins")
    val chapterNameMargins: Margins = Margins("ChapterNameMargins")
    val timeMargins: Margins = Margins("TimeMargins")
    val batteryMargins: Margins = Margins("BatteryMargins")
    /**
     * 对应上面几个，也就是页眉页脚那些信息的字体大小，
     */
    var messageSize: Int by PrimitiveDelegate(12)

    var textColor: Int by PrimitiveDelegate(0xff000000.toInt())
    var font: Uri? by UriDelegate()
    val tfFont: Typeface?
        get() = font?.let {
            try {
                Typeface.createFromFile(it.path)
            } catch (e: Exception) {
                error("字体生成失败", e)
                null
            }
        }
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

    var animationMode: AnimationMode by GsonDelegate.new(AnimationMode.SIMPLE)
    var animationSpeed: Float by PrimitiveDelegate(0.8f)
    var fullScreenClickNextPage: Boolean by PrimitiveDelegate(false)
    var volumeKeyScroll: Boolean by PrimitiveDelegate(false)
    var centerPercent: Float by PrimitiveDelegate(0.5f)

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
            paginationMargins.enabled,
            bookNameMargins.enabled,
            chapterNameMargins.enabled,
            timeMargins.enabled,
            batteryMargins.enabled,
            messageSize,
            textColor,
            backgroundColor,
            backgroundImage,
            animationMode,
            animationSpeed,
            tfFont,
            centerPercent,
            fullScreenClickNextPage
    )
}

